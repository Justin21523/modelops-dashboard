package com.justin.modelops.benchmark.mapper;

import com.justin.modelops.benchmark.dto.BenchmarkResponse;
import com.justin.modelops.benchmark.dto.CreateBenchmarkRequest;
import com.justin.modelops.benchmark.dto.UpdateBenchmarkRequest;
import com.justin.modelops.benchmark.entity.BenchmarkDefinition;
import com.justin.modelops.tag.mapper.TagMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = TagMapper.class)
public interface BenchmarkMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    BenchmarkDefinition toEntity(CreateBenchmarkRequest request);

    BenchmarkResponse toResponse(BenchmarkDefinition benchmark);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateBenchmarkRequest request, @MappingTarget BenchmarkDefinition benchmark);
}
