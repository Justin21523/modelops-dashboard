package com.justin.modelops.dashboard.dto;

/**
 * Aggregated counters and averages shown on the dashboard landing view.
 */
public record DashboardSummaryResponse(
        long totalModels,
        long readyModels,
        long archivedModels,
        long totalTasks,
        long runningTasks,
        long succeededTasks,
        long failedTasks,
        Double averageLatencyMs,
        Double averageTokensPerSecond) {
}
