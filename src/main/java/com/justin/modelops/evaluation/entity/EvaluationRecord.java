package com.justin.modelops.evaluation.entity;

import com.justin.modelops.benchmark.entity.BenchmarkDefinition;
import com.justin.modelops.common.audit.BaseAuditEntity;
import com.justin.modelops.evaluation.enums.EvaluationStatus;
import com.justin.modelops.hardware.entity.HardwareProfile;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.runtime.entity.RuntimeBackend;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Benchmark / evaluation result linking a model to the hardware and runtime it was
 * measured on. Phase 1 defines the schema; the CRUD workflow is implemented in Phase 2.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "evaluation_records")
public class EvaluationRecord extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private AiModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hardware_profile_id")
    private HardwareProfile hardwareProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runtime_backend_id")
    private RuntimeBackend runtimeBackend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benchmark_id")
    private BenchmarkDefinition benchmark;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private EvaluationStatus status = EvaluationStatus.PENDING;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "tokens_per_second")
    private Double tokensPerSecond;

    @Column(name = "memory_usage_mb")
    private Integer memoryUsageMb;

    @Column(name = "score")
    private Double score;

    @Column(name = "notes", length = 2048)
    private String notes;
}
