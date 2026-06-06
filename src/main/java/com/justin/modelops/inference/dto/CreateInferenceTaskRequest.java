package com.justin.modelops.inference.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInferenceTaskRequest(
        @NotNull Long modelId,
        Long runtimeBackendId,
        @NotBlank @Size(max = 4096) String prompt,
        @Valid InferenceParameters parameters) {
}
