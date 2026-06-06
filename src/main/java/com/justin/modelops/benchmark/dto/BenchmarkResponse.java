package com.justin.modelops.benchmark.dto;

import com.justin.modelops.benchmark.enums.BenchmarkType;
import com.justin.modelops.tag.dto.TagSummary;

import java.time.Instant;
import java.util.Set;

public record BenchmarkResponse(
        Long id,
        String name,
        BenchmarkType type,
        String description,
        String scoringNotes,
        Set<TagSummary> tags,
        Instant createdAt,
        Instant updatedAt) {
}
