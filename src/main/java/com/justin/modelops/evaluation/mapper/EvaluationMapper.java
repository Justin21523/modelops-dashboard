package com.justin.modelops.evaluation.mapper;

import com.justin.modelops.evaluation.dto.EvaluationRecordResponse;
import com.justin.modelops.evaluation.entity.EvaluationRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationMapper {

    @Mapping(target = "modelId", source = "model.id")
    @Mapping(target = "modelName", source = "model.name")
    @Mapping(target = "hardwareProfileId", source = "hardwareProfile.id")
    @Mapping(target = "hardwareProfileName", source = "hardwareProfile.name")
    @Mapping(target = "runtimeBackendId", source = "runtimeBackend.id")
    @Mapping(target = "runtimeBackendName", source = "runtimeBackend.name")
    @Mapping(target = "benchmarkId", source = "benchmark.id")
    @Mapping(target = "benchmarkName", source = "benchmark.name")
    EvaluationRecordResponse toResponse(EvaluationRecord record);
}
