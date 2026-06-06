package com.justin.modelops.evaluation.dto;

import com.justin.modelops.evaluation.enums.EvaluationStatus;

import java.time.Instant;

public record EvaluationRecordResponse(
        Long id,
        Long modelId,
        String modelName,
        Long hardwareProfileId,
        String hardwareProfileName,
        Long runtimeBackendId,
        String runtimeBackendName,
        Long benchmarkId,
        String benchmarkName,
        EvaluationStatus status,
        Long latencyMs,
        Double tokensPerSecond,
        Integer memoryUsageMb,
        Double score,
        String notes,
        Instant createdAt,
        Instant updatedAt) {
}
