package com.justin.modelops.tag.mapper;

import com.justin.modelops.tag.dto.CreateTagRequest;
import com.justin.modelops.tag.dto.TagResponse;
import com.justin.modelops.tag.dto.TagSummary;
import com.justin.modelops.tag.dto.UpdateTagRequest;
import com.justin.modelops.tag.entity.Tag;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Tag toEntity(CreateTagRequest request);

    TagResponse toResponse(Tag tag);

    TagSummary toSummary(Tag tag);

    default Set<TagSummary> toSummaries(Set<Tag> tags) {
        return tags == null ? Set.of() : tags.stream().map(this::toSummary).collect(Collectors.toSet());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateTagRequest request, @MappingTarget Tag tag);
}
