package com.justin.modelops.evaluation.dto;

import com.justin.modelops.evaluation.enums.EvaluationStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Partial update payload. Relationship ids, when non-null, re-point the record; all
 * other non-null fields are applied.
 */
public record UpdateEvaluationRecordRequest(
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
