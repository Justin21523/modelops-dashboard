package com.justin.modelops.model.entity;

import com.justin.modelops.common.audit.BaseAuditEntity;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import com.justin.modelops.tag.entity.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

/**
 * Registered AI model metadata. Storage location is captured as a safe alias
 * ({@code storageNote}); real private host paths are never persisted.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_models")
public class AiModel extends BaseAuditEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "family", length = 96)
    private String family;

    @Column(name = "provider", length = 96)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", nullable = false, length = 32)
    private ModelModality modality;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "format_id", nullable = false)
    private ModelFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantization", nullable = false, length = 32)
    private QuantizationType quantization = QuantizationType.NONE;

    @Column(name = "parameter_size", length = 32)
    private String parameterSize;

    @Column(name = "estimated_vram_mb")
    private Integer estimatedVramMb;

    @Column(name = "license", length = 96)
    private String license;

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    /**
     * Safe, non-sensitive storage hint (e.g. {@code alias://models/llama3-8b}).
     */
    @Column(name = "storage_note", length = 256)
    private String storageNote;

    @Column(name = "notes", length = 1024)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ModelStatus status = ModelStatus.DRAFT;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "model_tags",
            joinColumns = @JoinColumn(name = "model_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @BatchSize(size = 50)
    private Set<Tag> tags = new HashSet<>();

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
}
