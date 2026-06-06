package com.justin.modelops.inference.adapter;

import com.justin.modelops.inference.entity.InferenceTask;
import com.justin.modelops.runtime.enums.BackendType;

/**
 * Strategy for executing an inference task against a specific runtime backend.
 *
 * <p>Phase 1 ships only {@link MockRuntimeAdapter}. Real adapters (llama.cpp, Ollama,
 * ONNX Runtime, DJL, ...) implement this interface in later phases without requiring
 * changes to the inference service.
 */
public interface RuntimeAdapter {

    /**
     * @return whether this adapter can execute tasks for the given backend type
     */
    boolean supports(BackendType backendType);

    /**
     * Executes the task synchronously and returns its result.
     *
     * @throws RuntimeException if execution fails; the caller marks the task FAILED
     */
    InferenceResult run(InferenceTask task);
}
