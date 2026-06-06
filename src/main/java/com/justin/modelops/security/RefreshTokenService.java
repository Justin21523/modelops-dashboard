package com.justin.modelops.security;

import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Issues, rotates, and revokes opaque DB-backed refresh tokens. The raw token is only
 * ever returned to the client; the database stores its SHA-256 hash.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    /** A freshly minted refresh token and its expiry, returned to the client once. */
    public record IssuedToken(String rawToken, Instant expiresAt) {
    }

    /** Result of rotating a refresh token: the owner plus the replacement token. */
    public record RotationResult(User user, IssuedToken token) {
    }

    @Transactional
    public IssuedToken issue(User user) {
        String rawToken = generateRawToken();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setCreatedAt(Instant.now());
        entity.setExpiresAt(Instant.now().plus(jwtProperties.refreshTokenExpiration()));
        repository.save(entity);
        return new IssuedToken(rawToken, entity.getExpiresAt());
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        RefreshToken existing = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid refresh token"));
        if (!existing.isActive(Instant.now())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Refresh token expired or revoked");
        }
        existing.setRevokedAt(Instant.now());
        User user = existing.getUser();
        return new RotationResult(user, issue(user));
    }

    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    @Transactional
    public void revokeAllForUser(User user) {
        repository.revokeAllForUser(user, Instant.now());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
