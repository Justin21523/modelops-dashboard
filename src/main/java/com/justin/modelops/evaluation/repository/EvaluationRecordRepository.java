package com.justin.modelops.evaluation.repository;

import com.justin.modelops.evaluation.entity.EvaluationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationRecordRepository
        extends JpaRepository<EvaluationRecord, Long>, JpaSpecificationExecutor<EvaluationRecord> {

    /**
     * Aggregates evaluation metrics per model, optionally scoped to a single benchmark.
     * Each row: [modelId, modelName, count, avgScore, avgLatencyMs, avgTokensPerSecond].
     */
    @Query("""
            select e.model.id, e.model.name, count(e), avg(e.score), avg(e.latencyMs), avg(e.tokensPerSecond)
            from EvaluationRecord e
            where (:benchmarkId is null or e.benchmark.id = :benchmarkId)
            group by e.model.id, e.model.name
            order by avg(e.score) desc nulls last
            """)
    List<Object[]> aggregateByModel(@Param("benchmarkId") Long benchmarkId);
}
