package com.justin.modelops.user.dto;

import jakarta.validation.constraints.Size;

/**
 * Partial profile update; only non-null fields are applied.
 */
public record UpdateProfileRequest(
        @Size(max = 128) String displayName) {
}
