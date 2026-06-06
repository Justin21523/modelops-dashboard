package com.justin.modelops.model.dto;

import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Partial update payload. All fields are optional; only non-null fields are applied.
 */
public record UpdateModelRequest(
        @Size(max = 128) String name,
        @Size(max = 96) String family,
        @Size(max = 96) String provider,
        ModelModality modality,
        ModelFormatType formatType,
        QuantizationType quantization,
        @Size(max = 32) String parameterSize,
        @PositiveOrZero Integer estimatedVramMb,
        @Size(max = 96) String license,
        @Size(max = 512) String sourceUrl,
        @Size(max = 256) String storageNote,
        @Size(max = 1024) String notes,
        ModelStatus status) {
}
