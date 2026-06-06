package com.justin.modelops.user.dto;

import java.time.Instant;
import java.util.Set;

/**
 * Public representation of a user. Never exposes the password hash.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        Set<String> roles,
        boolean enabled,
        Instant createdAt) {
}
