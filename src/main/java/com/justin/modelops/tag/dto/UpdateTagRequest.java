package com.justin.modelops.tag.dto;

import jakarta.validation.constraints.Size;

/**
 * Partial update payload; only non-null fields are applied.
 */
public record UpdateTagRequest(
        @Size(max = 64) String name,
        @Size(max = 16) String color,
        @Size(max = 256) String description) {
}
