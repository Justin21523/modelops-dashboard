package com.justin.modelops.tag.dto;

/**
 * Compact tag projection embedded in tagged resource responses (models, benchmarks,
 * inference tasks).
 */
public record TagSummary(Long id, String name, String color) {
}
