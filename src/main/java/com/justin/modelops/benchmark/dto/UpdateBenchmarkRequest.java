package com.justin.modelops.benchmark.dto;

import com.justin.modelops.benchmark.enums.BenchmarkType;
import jakarta.validation.constraints.Size;

/**
 * Partial update payload; only non-null fields are applied.
 */
public record UpdateBenchmarkRequest(
        @Size(max = 128) String name,
        BenchmarkType type,
        @Size(max = 1024) String description,
        @Size(max = 1024) String scoringNotes) {
}
