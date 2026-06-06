package com.justin.modelops.auth.service;

import com.justin.modelops.auth.dto.AuthResponse;
import com.justin.modelops.auth.dto.LoginRequest;
import com.justin.modelops.auth.dto.RegisterRequest;
import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.security.JwtProperties;
import com.justin.modelops.security.JwtTokenProvider;
import com.justin.modelops.user.entity.Role;
import com.justin.modelops.user.entity.User;
import com.justin.modelops.user.enums.UserRole;
import com.justin.modelops.user.mapper.UserMapper;
import com.justin.modelops.user.repository.RoleRepository;
import com.justin.modelops.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles registration and login: password hashing, default role assignment, and
 * JWT issuance. Refresh tokens are deferred to Phase 2.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Email already registered: " + request.email());
        }

        Role userRole = roleRepository.findByName(UserRole.USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Default role USER is not configured"));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.addRole(userRole);
        User saved = userRepository.save(user);

        auditService.record(AuditAction.REGISTER, "User", saved.getId());
        return issueToken(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid credentials"));
            auditService.record(AuditAction.LOGIN, "User", user.getId());
            return issueToken(user);
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid username or password");
        }
    }

    private AuthResponse issueToken(User user) {
        List<String> authorities = user.getRoles().stream().map(role -> role.getName().authority()).toList();
        String token = tokenProvider.generateAccessToken(user.getUsername(), authorities);
        return AuthResponse.bearer(token, jwtProperties.accessTokenExpiration().toSeconds(),
                userMapper.toResponse(user));
    }
}
