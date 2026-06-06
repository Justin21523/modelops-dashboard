package com.justin.modelops.hardware.mapper;

import com.justin.modelops.hardware.dto.CreateHardwareProfileRequest;
import com.justin.modelops.hardware.dto.HardwareProfileResponse;
import com.justin.modelops.hardware.dto.UpdateHardwareProfileRequest;
import com.justin.modelops.hardware.entity.HardwareProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HardwareProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    HardwareProfile toEntity(CreateHardwareProfileRequest request);

    HardwareProfileResponse toResponse(HardwareProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateHardwareProfileRequest request, @MappingTarget HardwareProfile profile);
}
