package com.justin.modelops.model.entity;

import com.justin.modelops.model.enums.ModelFormatType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reference lookup describing a supported model format. Seeded by Flyway and
 * referenced by {@link AiModel}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "model_formats")
public class ModelFormat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true, length = 32)
    private ModelFormatType type;

    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;

    @Column(name = "file_extension", length = 32)
    private String fileExtension;

    @Column(name = "description", length = 256)
    private String description;
}
