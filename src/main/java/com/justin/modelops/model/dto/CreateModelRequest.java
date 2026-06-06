package com.justin.modelops.model.dto;

import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateModelRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 96) String family,
        @Size(max = 96) String provider,
        @NotNull ModelModality modality,
        @NotNull ModelFormatType formatType,
        QuantizationType quantization,
        @Size(max = 32) String parameterSize,
        @PositiveOrZero Integer estimatedVramMb,
        @Size(max = 96) String license,
        @Size(max = 512) String sourceUrl,
        @Size(max = 256) String storageNote,
        @Size(max = 1024) String notes,
        ModelStatus status) {
}
