package com.justin.modelops.dashboard.dto;

/**
 * A model ranked by average inference throughput across its succeeded tasks.
 */
public record FastestModelEntry(
        Long modelId,
        String modelName,
        Double avgTokensPerSecond,
        Double avgLatencyMs,
        long taskCount) {
}
