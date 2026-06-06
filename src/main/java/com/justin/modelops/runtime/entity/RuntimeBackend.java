package com.justin.modelops.runtime.entity;

import com.justin.modelops.common.audit.BaseAuditEntity;
import com.justin.modelops.runtime.enums.BackendStatus;
import com.justin.modelops.runtime.enums.BackendType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A configured inference runtime backend (endpoint + capabilities + status).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "runtime_backends")
public class RuntimeBackend extends BaseAuditEntity {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "backend_type", nullable = false, length = 32)
    private BackendType backendType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BackendStatus status = BackendStatus.UNKNOWN;

    @Column(name = "endpoint_url", length = 512)
    private String endpointUrl;

    @Column(name = "capabilities", length = 512)
    private String capabilities;

    @Column(name = "description", length = 512)
    private String description;
}
