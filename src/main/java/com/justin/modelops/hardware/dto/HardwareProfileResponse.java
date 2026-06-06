package com.justin.modelops.hardware.dto;

import com.justin.modelops.hardware.enums.HardwareBackendType;

import java.time.Instant;

public record HardwareProfileResponse(
        Long id,
        String name,
        String gpuModel,
        Integer vramMb,
        Integer ramMb,
        HardwareBackendType backendType,
        String operatingSystem,
        String driverNotes,
        Instant createdAt,
        Instant updatedAt) {
}
