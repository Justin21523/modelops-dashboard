package com.justin.modelops.runtime.dto;

import com.justin.modelops.runtime.enums.BackendStatus;
import com.justin.modelops.runtime.enums.BackendType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRuntimeBackendRequest(
        @NotBlank @Size(max = 128) String name,
        @NotNull BackendType backendType,
        BackendStatus status,
        @Size(max = 512) String endpointUrl,
        @Size(max = 512) String capabilities,
        @Size(max = 512) String description) {
}
