package com.justin.modelops.inference.dto;

import com.justin.modelops.inference.enums.InferenceTaskStatus;

import java.time.Instant;

public record InferenceTaskResponse(
        Long id,
        Long modelId,
        String modelName,
        Long runtimeBackendId,
        String runtimeBackendName,
        InferenceTaskStatus status,
        String prompt,
        String parameters,
        String outputSummary,
        Long latencyMs,
        Double tokensPerSecond,
        String errorMessage,
        Instant startedAt,
        Instant finishedAt,
        Instant createdAt) {
}
