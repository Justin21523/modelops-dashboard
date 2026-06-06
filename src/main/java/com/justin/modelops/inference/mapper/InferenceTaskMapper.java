package com.justin.modelops.inference.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.modelops.inference.dto.InferenceParameters;
import com.justin.modelops.inference.dto.InferenceTaskResponse;
import com.justin.modelops.inference.entity.InferenceTask;
import com.justin.modelops.tag.mapper.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Maps inference tasks to responses, deserializing the JSON {@code parameters} column
 * into a structured {@link InferenceParameters} and projecting tags via {@link TagMapper}.
 */
@Mapper(componentModel = "spring", uses = TagMapper.class)
public abstract class InferenceTaskMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "modelId", source = "model.id")
    @Mapping(target = "modelName", source = "model.name")
    @Mapping(target = "runtimeBackendId", source = "runtimeBackend.id")
    @Mapping(target = "runtimeBackendName", source = "runtimeBackend.name")
    @Mapping(target = "parameters", source = "parameters", qualifiedByName = "parseParameters")
    public abstract InferenceTaskResponse toResponse(InferenceTask task);

    @Named("parseParameters")
    protected InferenceParameters parseParameters(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, InferenceParameters.class);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
