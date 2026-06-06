package com.justin.modelops.inference.repository;

import com.justin.modelops.inference.entity.InferenceTask;
import com.justin.modelops.inference.enums.InferenceTaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InferenceTaskRepository
        extends JpaRepository<InferenceTask, Long>, JpaSpecificationExecutor<InferenceTask> {

    long countByStatus(InferenceTaskStatus status);

    List<InferenceTask> findTop10ByOrderByCreatedAtDesc();

    Page<InferenceTask> findByStatus(InferenceTaskStatus status, Pageable pageable);

    @Query("select avg(t.latencyMs) from InferenceTask t where t.status = "
            + "com.justin.modelops.inference.enums.InferenceTaskStatus.SUCCEEDED")
    Double averageLatencyMsForSucceeded();

    @Query("select avg(t.tokensPerSecond) from InferenceTask t where t.status = "
            + "com.justin.modelops.inference.enums.InferenceTaskStatus.SUCCEEDED")
    Double averageTokensPerSecondForSucceeded();

    /**
     * Ranks models by average throughput across their succeeded tasks.
     * Each row: [modelId, modelName, avgTokensPerSecond, avgLatencyMs, taskCount].
     */
    @Query("""
            select t.model.id, t.model.name, avg(t.tokensPerSecond), avg(t.latencyMs), count(t)
            from InferenceTask t
            where t.status = com.justin.modelops.inference.enums.InferenceTaskStatus.SUCCEEDED
            group by t.model.id, t.model.name
            order by avg(t.tokensPerSecond) desc nulls last
            """)
    List<Object[]> findFastestModels(Pageable pageable);
}
