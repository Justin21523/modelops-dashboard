package com.justin.modelops.inference.adapter;

/**
 * Outcome produced by a {@link RuntimeAdapter} for a single inference run.
 *
 * @param outputSummary    short summary of the generated output
 * @param latencyMs        wall-clock latency in milliseconds
 * @param tokensPerSecond  throughput estimate
 */
public record InferenceResult(String outputSummary, long latencyMs, double tokensPerSecond) {
}
