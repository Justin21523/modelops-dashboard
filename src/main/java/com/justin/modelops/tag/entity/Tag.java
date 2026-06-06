package com.justin.modelops.tag.entity;

import com.justin.modelops.common.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reusable label. Phase 1 persists tags only; associations to models, benchmarks, and
 * tasks are introduced in Phase 2.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tags")
public class Tag extends BaseAuditEntity {

    @Column(name = "name", nullable = false, unique = true, length = 64)
    private String name;

    @Column(name = "color", length = 16)
    private String color;

    @Column(name = "description", length = 256)
    private String description;
}
