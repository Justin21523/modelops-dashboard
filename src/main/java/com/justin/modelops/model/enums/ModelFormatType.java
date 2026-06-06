package com.justin.modelops.model.enums;

/**
 * On-disk serialization format of a model artifact.
 */
public enum ModelFormatType {
    GGUF,
    GGML,
    ONNX,
    SAFETENSORS,
    PYTORCH,
    TENSORFLOW,
    OTHER
}
