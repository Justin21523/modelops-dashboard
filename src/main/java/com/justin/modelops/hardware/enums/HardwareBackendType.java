package com.justin.modelops.hardware.enums;

/**
 * Compute backend a hardware profile is optimised for.
 */
public enum HardwareBackendType {
    CUDA,
    ROCM,
    CPU,
    METAL,
    OPENVINO,
    OTHER
}
