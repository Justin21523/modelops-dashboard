package com.justin.modelops.dashboard.dto;

import java.util.Map;

/**
 * Counts of registered models grouped by modality and by format type.
 */
public record ModelDistributionResponse(
        Map<String, Long> byModality,
        Map<String, Long> byFormat) {
}
