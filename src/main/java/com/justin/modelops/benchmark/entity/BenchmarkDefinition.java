package com.justin.modelops.benchmark.entity;

import com.justin.modelops.benchmark.enums.BenchmarkType;
import com.justin.modelops.common.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reusable benchmark definition. Phase 1 persists definitions only; the evaluation
 * workflow that consumes them is implemented in Phase 2.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "benchmark_definitions")
public class BenchmarkDefinition extends BaseAuditEntity {

    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private BenchmarkType type;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "scoring_notes", length = 1024)
    private String scoringNotes;
}
