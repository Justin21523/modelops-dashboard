package com.justin.modelops.inference.mapper;

import com.justin.modelops.inference.dto.InferenceTaskResponse;
import com.justin.modelops.inference.entity.InferenceTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InferenceTaskMapper {

    @Mapping(target = "modelId", source = "model.id")
    @Mapping(target = "modelName", source = "model.name")
    @Mapping(target = "runtimeBackendId", source = "runtimeBackend.id")
    @Mapping(target = "runtimeBackendName", source = "runtimeBackend.name")
    InferenceTaskResponse toResponse(InferenceTask task);
}
