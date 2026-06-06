package com.justin.modelops.user.mapper;

import com.justin.modelops.user.entity.Role;
import com.justin.modelops.user.entity.User;
import com.justin.modelops.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @org.mapstruct.Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    UserResponse toResponse(User user);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    }
}
