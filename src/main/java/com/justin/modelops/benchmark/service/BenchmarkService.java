package com.justin.modelops.benchmark.service;

import com.justin.modelops.benchmark.dto.BenchmarkFilter;
import com.justin.modelops.benchmark.dto.BenchmarkResponse;
import com.justin.modelops.benchmark.dto.CreateBenchmarkRequest;
import com.justin.modelops.benchmark.dto.UpdateBenchmarkRequest;
import com.justin.modelops.benchmark.entity.BenchmarkDefinition;
import com.justin.modelops.benchmark.mapper.BenchmarkMapper;
import com.justin.modelops.benchmark.repository.BenchmarkDefinitionRepository;
import com.justin.modelops.benchmark.repository.BenchmarkSpecifications;
import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.tag.entity.Tag;
import com.justin.modelops.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BenchmarkService {

    private final BenchmarkDefinitionRepository repository;
    private final TagRepository tagRepository;
    private final BenchmarkMapper mapper;
    private final AuditService auditService;

    @Transactional
    public BenchmarkResponse create(CreateBenchmarkRequest request) {
        if (repository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Benchmark already exists: " + request.name());
        }
        BenchmarkDefinition saved = repository.save(mapper.toEntity(request));
        auditService.record(AuditAction.CREATE, "BenchmarkDefinition", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<BenchmarkResponse> list(BenchmarkFilter filter, Pageable pageable) {
        return repository.findAll(BenchmarkSpecifications.withFilter(filter), pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BenchmarkResponse get(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public BenchmarkResponse update(Long id, UpdateBenchmarkRequest request) {
        BenchmarkDefinition benchmark = getEntity(id);
        if (request.name() != null && !request.name().equals(benchmark.getName())
                && repository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Benchmark already exists: " + request.name());
        }
        mapper.updateEntity(request, benchmark);
        auditService.record(AuditAction.UPDATE, "BenchmarkDefinition", id);
        return mapper.toResponse(benchmark);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
        auditService.record(AuditAction.DELETE, "BenchmarkDefinition", id);
    }

    @Transactional
    public BenchmarkResponse attachTag(Long id, Long tagId) {
        BenchmarkDefinition benchmark = getEntity(id);
        benchmark.addTag(resolveTag(tagId));
        auditService.record(AuditAction.UPDATE, "BenchmarkDefinition", id, "attach tag " + tagId);
        return mapper.toResponse(benchmark);
    }

    @Transactional
    public BenchmarkResponse detachTag(Long id, Long tagId) {
        BenchmarkDefinition benchmark = getEntity(id);
        benchmark.removeTag(resolveTag(tagId));
        auditService.record(AuditAction.UPDATE, "BenchmarkDefinition", id, "detach tag " + tagId);
        return mapper.toResponse(benchmark);
    }

    private Tag resolveTag(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));
    }

    private BenchmarkDefinition getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BenchmarkDefinition", id));
    }
}
