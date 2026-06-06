package com.justin.modelops.model.dto;

import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import com.justin.modelops.tag.dto.TagSummary;

import java.time.Instant;
import java.util.Set;

public record ModelResponse(
        Long id,
        String name,
        String family,
        String provider,
        ModelModality modality,
        ModelFormatType formatType,
        QuantizationType quantization,
        String parameterSize,
        Integer estimatedVramMb,
        String license,
        String sourceUrl,
        String storageNote,
        String notes,
        ModelStatus status,
        Set<TagSummary> tags,
        Instant createdAt,
        Instant updatedAt) {
}
