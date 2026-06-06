package com.justin.modelops.evaluation.dto;

/**
 * Per-model aggregate of evaluation metrics, optionally scoped to one benchmark.
 */
public record EvaluationAggregateResponse(
        Long modelId,
        String modelName,
        long count,
        Double avgScore,
        Double avgLatencyMs,
        Double avgTokensPerSecond) {
}
