package com.justin.modelops.evaluation.dto;

import com.justin.modelops.evaluation.enums.EvaluationStatus;

/**
 * Optional query filters for the evaluation list endpoint. Any null field is ignored.
 */
public record EvaluationFilter(
        Long modelId,
        Long benchmarkId,
        Long hardwareProfileId,
        Long runtimeBackendId,
        EvaluationStatus status) {
}
