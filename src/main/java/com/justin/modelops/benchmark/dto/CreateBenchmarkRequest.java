package com.justin.modelops.benchmark.dto;

import com.justin.modelops.benchmark.enums.BenchmarkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBenchmarkRequest(
        @NotBlank @Size(max = 128) String name,
        @NotNull BenchmarkType type,
        @Size(max = 1024) String description,
        @Size(max = 1024) String scoringNotes) {
}
