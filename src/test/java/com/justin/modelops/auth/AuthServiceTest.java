package com.justin.modelops.auth;

import com.justin.modelops.auth.dto.AuthResponse;
import com.justin.modelops.auth.dto.LoginRequest;
import com.justin.modelops.auth.dto.RegisterRequest;
import com.justin.modelops.auth.service.AuthService;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.security.JwtProperties;
import com.justin.modelops.security.JwtTokenProvider;
import com.justin.modelops.user.dto.UserResponse;
import com.justin.modelops.user.entity.Role;
import com.justin.modelops.user.entity.User;
import com.justin.modelops.user.enums.UserRole;
import com.justin.modelops.user.mapper.UserMapper;
import com.justin.modelops.user.repository.RoleRepository;
import com.justin.modelops.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuditService auditService;

    private final JwtProperties jwtProperties =
            new JwtProperties("secret", Duration.ofHours(1), "modelops-test");

    private AuthService authService;

    private AuthService service() {
        return new AuthService(userRepository, roleRepository, passwordEncoder, tokenProvider,
                jwtProperties, authenticationManager, userMapper, auditService);
    }

    @Test
    void register_hashesPasswordAndIssuesToken() {
        authService = service();
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "supersecret", "Alice");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepository.findByName(UserRole.USER)).thenReturn(Optional.of(new Role(UserRole.USER)));
        when(passwordEncoder.encode("supersecret")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateAccessToken(eq("alice"), anyList())).thenReturn("jwt-token");
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(new UserResponse(1L, "alice", "alice@example.com", "Alice",
                        Set.of("USER"), true, null));

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(captor.getValue().getRoles()).extracting(Role::getName).containsExactly(UserRole.USER);
    }

    @Test
    void register_rejectsDuplicateUsername() {
        authService = service();
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("alice", "alice@example.com", "supersecret", "Alice")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_CONFLICT);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        authService = service();
        User user = new User();
        user.setUsername("alice");
        user.addRole(new Role(UserRole.USER));
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", "supersecret");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(eq("alice"), anyList())).thenReturn("jwt-token");
        when(userMapper.toResponse(user))
                .thenReturn(new UserResponse(1L, "alice", "alice@example.com", "Alice",
                        Set.of("USER"), true, null));

        AuthResponse response = authService.login(new LoginRequest("alice", "supersecret"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        verify(auditService).record(any(), eq("User"), any());
    }

    @Test
    void login_rejectsBadCredentials() {
        authService = service();
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);

        verify(tokenProvider, never()).generateAccessToken(anyString(), anyList());
    }
}
