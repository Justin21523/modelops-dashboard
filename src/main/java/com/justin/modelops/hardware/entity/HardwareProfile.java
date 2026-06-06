package com.justin.modelops.hardware.entity;

import com.justin.modelops.common.audit.BaseAuditEntity;
import com.justin.modelops.hardware.enums.HardwareBackendType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A hardware configuration used to run inference, e.g. a specific GPU + OS combination.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "hardware_profiles")
public class HardwareProfile extends BaseAuditEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "gpu_model", length = 128)
    private String gpuModel;

    @Column(name = "vram_mb")
    private Integer vramMb;

    @Column(name = "ram_mb")
    private Integer ramMb;

    @Enumerated(EnumType.STRING)
    @Column(name = "backend_type", nullable = false, length = 32)
    private HardwareBackendType backendType;

    @Column(name = "operating_system", length = 96)
    private String operatingSystem;

    @Column(name = "driver_notes", length = 512)
    private String driverNotes;
}
