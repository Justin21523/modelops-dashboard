package com.justin.modelops.runtime.dto;

import com.justin.modelops.runtime.enums.BackendStatus;
import com.justin.modelops.runtime.enums.BackendType;
import jakarta.validation.constraints.Size;

/**
 * Partial update payload; only non-null fields are applied.
 */
public record UpdateRuntimeBackendRequest(
        @Size(max = 128) String name,
        BackendType backendType,
        BackendStatus status,
        @Size(max = 512) String endpointUrl,
        @Size(max = 512) String capabilities,
        @Size(max = 512) String description) {
}
