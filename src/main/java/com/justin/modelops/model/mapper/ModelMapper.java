package com.justin.modelops.model.mapper;

import com.justin.modelops.model.dto.ModelResponse;
import com.justin.modelops.model.dto.UpdateModelRequest;
import com.justin.modelops.model.entity.AiModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ModelMapper {

    @Mapping(target = "formatType", source = "format.type")
    ModelResponse toResponse(AiModel model);

    /**
     * Applies non-null fields from a patch request onto an existing entity.
     * {@code format} is resolved separately in the service from {@code formatType}.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "format", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateModelRequest request, @MappingTarget AiModel model);
}
