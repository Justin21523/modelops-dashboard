package com.justin.modelops.hardware.service;

import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.hardware.dto.CreateHardwareProfileRequest;
import com.justin.modelops.hardware.dto.HardwareProfileResponse;
import com.justin.modelops.hardware.dto.UpdateHardwareProfileRequest;
import com.justin.modelops.hardware.entity.HardwareProfile;
import com.justin.modelops.hardware.mapper.HardwareProfileMapper;
import com.justin.modelops.hardware.repository.HardwareProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HardwareProfileService {

    private final HardwareProfileRepository repository;
    private final HardwareProfileMapper mapper;
    private final AuditService auditService;

    @Transactional
    public HardwareProfileResponse create(CreateHardwareProfileRequest request) {
        HardwareProfile saved = repository.save(mapper.toEntity(request));
        auditService.record(AuditAction.CREATE, "HardwareProfile", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<HardwareProfileResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public HardwareProfileResponse get(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public HardwareProfileResponse update(Long id, UpdateHardwareProfileRequest request) {
        HardwareProfile profile = getEntity(id);
        mapper.updateEntity(request, profile);
        auditService.record(AuditAction.UPDATE, "HardwareProfile", id);
        return mapper.toResponse(profile);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
        auditService.record(AuditAction.DELETE, "HardwareProfile", id);
    }

    private HardwareProfile getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HardwareProfile", id));
    }
}
