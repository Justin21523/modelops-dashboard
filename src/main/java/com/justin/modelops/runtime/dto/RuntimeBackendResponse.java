package com.justin.modelops.runtime.dto;

import com.justin.modelops.runtime.enums.BackendStatus;
import com.justin.modelops.runtime.enums.BackendType;

import java.time.Instant;

public record RuntimeBackendResponse(
        Long id,
        String name,
        BackendType backendType,
        BackendStatus status,
        String endpointUrl,
        String capabilities,
        String description,
        Instant createdAt,
        Instant updatedAt) {
}
