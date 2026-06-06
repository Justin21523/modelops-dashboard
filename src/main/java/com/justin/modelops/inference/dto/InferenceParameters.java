package com.justin.modelops.inference.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Structured sampling/generation parameters for an inference task. Persisted as JSON in
 * the task's {@code parameters} column. All fields are optional.
 */
public record InferenceParameters(
        @DecimalMin("0.0") @DecimalMax("2.0") Double temperature,
        @DecimalMin("0.0") @DecimalMax("1.0") Double topP,
        @Positive Integer maxTokens,
        Long seed,
        List<String> stop) {
}
