package com.justin.modelops.user.repository;

import com.justin.modelops.user.entity.Role;
import com.justin.modelops.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(UserRole name);
}
