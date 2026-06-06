package com.justin.modelops.runtime.service;

import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.runtime.dto.CreateRuntimeBackendRequest;
import com.justin.modelops.runtime.dto.RuntimeBackendResponse;
import com.justin.modelops.runtime.dto.UpdateRuntimeBackendRequest;
import com.justin.modelops.runtime.entity.RuntimeBackend;
import com.justin.modelops.runtime.enums.BackendStatus;
import com.justin.modelops.runtime.mapper.RuntimeBackendMapper;
import com.justin.modelops.runtime.repository.RuntimeBackendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RuntimeBackendService {

    private final RuntimeBackendRepository repository;
    private final RuntimeBackendMapper mapper;
    private final AuditService auditService;

    @Transactional
    public RuntimeBackendResponse create(CreateRuntimeBackendRequest request) {
        RuntimeBackend backend = mapper.toEntity(request);
        backend.setStatus(request.status() == null ? BackendStatus.UNKNOWN : request.status());
        RuntimeBackend saved = repository.save(backend);
        auditService.record(AuditAction.CREATE, "RuntimeBackend", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<RuntimeBackendResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RuntimeBackendResponse get(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public RuntimeBackendResponse update(Long id, UpdateRuntimeBackendRequest request) {
        RuntimeBackend backend = getEntity(id);
        mapper.updateEntity(request, backend);
        auditService.record(AuditAction.UPDATE, "RuntimeBackend", id);
        return mapper.toResponse(backend);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
        auditService.record(AuditAction.DELETE, "RuntimeBackend", id);
    }

    private RuntimeBackend getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RuntimeBackend", id));
    }
}
