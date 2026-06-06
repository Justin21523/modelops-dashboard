package com.justin.modelops.inference.dto;

import com.justin.modelops.inference.enums.InferenceTaskStatus;
import com.justin.modelops.tag.dto.TagSummary;

import java.time.Instant;
import java.util.Set;

public record InferenceTaskResponse(
        Long id,
        Long modelId,
        String modelName,
        Long runtimeBackendId,
        String runtimeBackendName,
        InferenceTaskStatus status,
        String prompt,
        InferenceParameters parameters,
        String outputSummary,
        Long latencyMs,
        Double tokensPerSecond,
        String errorMessage,
        Set<TagSummary> tags,
        Instant startedAt,
        Instant finishedAt,
        Instant createdAt) {
}
