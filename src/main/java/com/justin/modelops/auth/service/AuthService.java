package com.justin.modelops.auth.service;

import com.justin.modelops.auth.dto.AuthResponse;
import com.justin.modelops.auth.dto.LoginRequest;
import com.justin.modelops.auth.dto.RefreshTokenRequest;
import com.justin.modelops.auth.dto.RegisterRequest;
import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.security.JwtProperties;
import com.justin.modelops.security.JwtTokenProvider;
import com.justin.modelops.security.RefreshTokenService;
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
 * Handles registration, login, and refresh-token lifecycle: password hashing, default
 * role assignment, JWT issuance, and DB-backed refresh tokens with rotation/revocation.
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
    private final RefreshTokenService refreshTokenService;
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
        return issueTokens(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid credentials"));
            auditService.record(AuditAction.LOGIN, "User", user.getId());
            return issueTokens(user);
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(request.refreshToken());
        User user = rotation.user();
        auditService.record(AuditAction.REFRESH, "User", user.getId());
        return buildResponse(user, rotation.token());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        auditService.record(AuditAction.LOGOUT, "User", null);
    }

    private AuthResponse issueTokens(User user) {
        return buildResponse(user, refreshTokenService.issue(user));
    }

    private AuthResponse buildResponse(User user, RefreshTokenService.IssuedToken refreshToken) {
        List<String> authorities = user.getRoles().stream().map(role -> role.getName().authority()).toList();
        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), authorities);
        long refreshSeconds = jwtProperties.refreshTokenExpiration().toSeconds();
        return AuthResponse.bearer(accessToken, jwtProperties.accessTokenExpiration().toSeconds(),
                refreshToken.rawToken(), refreshSeconds, userMapper.toResponse(user));
    }
}
