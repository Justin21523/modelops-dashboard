package com.justin.modelops.notification.dto;

import com.justin.modelops.inference.enums.InferenceTaskStatus;

import java.time.Instant;

/**
 * Real-time message broadcast over WebSocket when an inference task changes state.
 */
public record TaskStatusMessage(
        Long taskId,
        Long modelId,
        InferenceTaskStatus status,
        String message,
        Long latencyMs,
        Double tokensPerSecond,
        Instant timestamp) {

    public static TaskStatusMessage of(Long taskId, Long modelId, InferenceTaskStatus status, String message) {
        return new TaskStatusMessage(taskId, modelId, status, message, null, null, Instant.now());
    }

    public static TaskStatusMessage completed(Long taskId, Long modelId, InferenceTaskStatus status,
                                              String message, Long latencyMs, Double tokensPerSecond) {
        return new TaskStatusMessage(taskId, modelId, status, message, latencyMs, tokensPerSecond, Instant.now());
    }
}
