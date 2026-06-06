package com.justin.modelops.tag.dto;

import java.time.Instant;

public record TagResponse(
        Long id,
        String name,
        String color,
        String description,
        Instant createdAt,
        Instant updatedAt) {
}
