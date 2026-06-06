package com.justin.modelops.model.dto;

import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;

/**
 * Optional query filters for the model list endpoint. Any null field is ignored.
 *
 * @param keyword case-insensitive match against name, family, and provider
 */
public record ModelFilter(
        ModelModality modality,
        ModelFormatType formatType,
        ModelStatus status,
        String keyword,
        Long tagId) {
}
