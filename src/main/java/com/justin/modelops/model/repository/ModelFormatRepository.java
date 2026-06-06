package com.justin.modelops.model.repository;

import com.justin.modelops.model.entity.ModelFormat;
import com.justin.modelops.model.enums.ModelFormatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelFormatRepository extends JpaRepository<ModelFormat, Long> {

    Optional<ModelFormat> findByType(ModelFormatType type);
}
