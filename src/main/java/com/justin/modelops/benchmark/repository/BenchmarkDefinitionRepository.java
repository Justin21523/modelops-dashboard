package com.justin.modelops.benchmark.repository;

import com.justin.modelops.benchmark.entity.BenchmarkDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BenchmarkDefinitionRepository
        extends JpaRepository<BenchmarkDefinition, Long>, JpaSpecificationExecutor<BenchmarkDefinition> {

    boolean existsByName(String name);
}
