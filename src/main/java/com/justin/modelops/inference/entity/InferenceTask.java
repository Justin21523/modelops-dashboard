package com.justin.modelops.inference.entity;

import com.justin.modelops.common.audit.BaseAuditEntity;
import com.justin.modelops.inference.enums.InferenceTaskStatus;
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

import java.time.Instant;

/**
 * A single inference request and its execution outcome. In Phase 1 execution is
 * simulated by the mock runtime adapter.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inference_tasks")
public class InferenceTask extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private AiModel model;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "runtime_backend_id")
    private RuntimeBackend runtimeBackend;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private InferenceTaskStatus status = InferenceTaskStatus.QUEUED;

    @Column(name = "prompt", nullable = false, length = 4096)
    private String prompt;

    @Column(name = "parameters", length = 2048)
    private String parameters;

    @Column(name = "output_summary", length = 4096)
    private String outputSummary;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "tokens_per_second")
    private Double tokensPerSecond;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;
}
