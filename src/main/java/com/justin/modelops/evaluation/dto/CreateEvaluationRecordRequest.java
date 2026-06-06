package com.justin.modelops.evaluation.dto;

import com.justin.modelops.evaluation.enums.EvaluationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateEvaluationRecordRequest(
        @NotNull Long modelId,
        Long hardwareProfileId,
        Long runtimeBackendId,
        Long benchmarkId,
        EvaluationStatus status,
        @PositiveOrZero Long latencyMs,
        @PositiveOrZero Double tokensPerSecond,
        @PositiveOrZero Integer memoryUsageMb,
        Double score,
        @Size(max = 2048) String notes) {
}
