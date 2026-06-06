package com.justin.modelops.hardware.dto;

import com.justin.modelops.hardware.enums.HardwareBackendType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateHardwareProfileRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 128) String gpuModel,
        @PositiveOrZero Integer vramMb,
        @PositiveOrZero Integer ramMb,
        @NotNull HardwareBackendType backendType,
        @Size(max = 96) String operatingSystem,
        @Size(max = 512) String driverNotes) {
}
