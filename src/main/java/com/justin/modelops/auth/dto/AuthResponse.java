package com.justin.modelops.auth.dto;

import com.justin.modelops.user.dto.UserResponse;

/**
 * Returned on successful registration or login.
 *
 * @param accessToken signed JWT access token
 * @param tokenType   always {@code Bearer}
 * @param expiresInSeconds access token lifetime in seconds
 * @param user        the authenticated user's public profile
 */
public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {

    public static AuthResponse bearer(String accessToken, long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, user);
    }
}
