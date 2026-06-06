package com.justin.modelops.model.repository;

import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AiModelRepository extends JpaRepository<AiModel, Long>, JpaSpecificationExecutor<AiModel> {

    long countByStatus(ModelStatus status);

    long countByModality(ModelModality modality);

    @Query("select m.modality, count(m) from AiModel m group by m.modality")
    List<Object[]> countGroupedByModality();

    @Query("select m.format.type, count(m) from AiModel m group by m.format.type")
    List<Object[]> countGroupedByFormat();
}
