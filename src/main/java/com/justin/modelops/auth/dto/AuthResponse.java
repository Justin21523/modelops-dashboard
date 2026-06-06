package com.justin.modelops.auth.dto;

import com.justin.modelops.user.dto.UserResponse;

/**
 * Returned on successful registration, login, or token refresh.
 *
 * @param accessToken            signed JWT access token
 * @param tokenType              always {@code Bearer}
 * @param expiresInSeconds       access token lifetime in seconds
 * @param refreshToken           opaque refresh token (rotated on each refresh)
 * @param refreshExpiresInSeconds refresh token lifetime in seconds
 * @param user                   the authenticated user's public profile
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds,
        UserResponse user) {

    public static AuthResponse bearer(String accessToken, long expiresInSeconds,
                                      String refreshToken, long refreshExpiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds,
                refreshToken, refreshExpiresInSeconds, user);
    }
}
