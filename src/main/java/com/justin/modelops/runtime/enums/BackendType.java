package com.justin.modelops.runtime.enums;

/**
 * Type of inference runtime backend. {@code MOCK} is the Phase 1 simulated backend;
 * the remaining values are placeholders for real adapters added in later phases.
 */
public enum BackendType {
    MOCK,
    LLAMA_CPP,
    OLLAMA,
    ONNX_RUNTIME,
    DJL,
    PYTHON_WORKER,
    CUSTOM_HTTP
}
