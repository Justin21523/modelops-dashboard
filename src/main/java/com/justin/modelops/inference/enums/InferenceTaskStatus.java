package com.justin.modelops.inference.enums;

/**
 * Lifecycle states of an inference task.
 */
public enum InferenceTaskStatus {
    QUEUED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELED;

    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED || this == CANCELED;
    }
}
