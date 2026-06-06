package com.justin.modelops.inference.event;

/**
 * Published after an inference task has been accepted for execution. Handled
 * asynchronously by the executor once the enclosing transaction commits.
 */
public record InferenceTaskQueuedEvent(Long taskId) {
}
