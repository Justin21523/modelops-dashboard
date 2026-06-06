package com.justin.modelops.runtime.repository;

import com.justin.modelops.runtime.entity.RuntimeBackend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuntimeBackendRepository extends JpaRepository<RuntimeBackend, Long> {
}
