package com.justin.modelops.model.enums;

/**
 * Quantization scheme applied to a model's weights.
 */
public enum QuantizationType {
    NONE,
    FP16,
    FP8,
    INT8,
    INT4,
    Q4_K_M,
    Q5_K_M,
    Q6_K,
    Q8_0,
    OTHER
}
