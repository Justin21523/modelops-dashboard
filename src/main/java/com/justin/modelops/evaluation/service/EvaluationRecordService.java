package com.justin.modelops.evaluation.service;

import com.justin.modelops.benchmark.entity.BenchmarkDefinition;
import com.justin.modelops.benchmark.repository.BenchmarkDefinitionRepository;
import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.config.RedisConfig;
import com.justin.modelops.evaluation.dto.CreateEvaluationRecordRequest;
import com.justin.modelops.evaluation.dto.EvaluationAggregateResponse;
import com.justin.modelops.evaluation.dto.EvaluationFilter;
import com.justin.modelops.evaluation.dto.EvaluationRecordResponse;
import com.justin.modelops.evaluation.dto.UpdateEvaluationRecordRequest;
import com.justin.modelops.evaluation.entity.EvaluationRecord;
import com.justin.modelops.evaluation.enums.EvaluationStatus;
import com.justin.modelops.evaluation.mapper.EvaluationMapper;
import com.justin.modelops.evaluation.repository.EvaluationRecordRepository;
import com.justin.modelops.evaluation.repository.EvaluationRecordSpecifications;
import com.justin.modelops.hardware.repository.HardwareProfileRepository;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.runtime.repository.RuntimeBackendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationRecordService {

    private final EvaluationRecordRepository repository;
    private final AiModelRepository modelRepository;
    private final HardwareProfileRepository hardwareRepository;
    private final RuntimeBackendRepository runtimeRepository;
    private final BenchmarkDefinitionRepository benchmarkRepository;
    private final EvaluationMapper mapper;
    private final AuditService auditService;

    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public EvaluationRecordResponse create(CreateEvaluationRecordRequest request) {
        EvaluationRecord record = new EvaluationRecord();
        record.setModel(modelRepository.findById(request.modelId())
                .orElseThrow(() -> new ResourceNotFoundException("AiModel", request.modelId())));
        if (request.hardwareProfileId() != null) {
            record.setHardwareProfile(hardwareRepository.findById(request.hardwareProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("HardwareProfile", request.hardwareProfileId())));
        }
        if (request.runtimeBackendId() != null) {
            record.setRuntimeBackend(runtimeRepository.findById(request.runtimeBackendId())
                    .orElseThrow(() -> new ResourceNotFoundException("RuntimeBackend", request.runtimeBackendId())));
        }
        if (request.benchmarkId() != null) {
            record.setBenchmark(resolveBenchmark(request.benchmarkId()));
        }
        record.setStatus(request.status() == null ? EvaluationStatus.PENDING : request.status());
        record.setLatencyMs(request.latencyMs());
        record.setTokensPerSecond(request.tokensPerSecond());
        record.setMemoryUsageMb(request.memoryUsageMb());
        record.setScore(request.score());
        record.setNotes(request.notes());

        EvaluationRecord saved = repository.save(record);
        auditService.record(AuditAction.CREATE, "EvaluationRecord", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<EvaluationRecordResponse> list(EvaluationFilter filter, Pageable pageable) {
        return repository.findAll(EvaluationRecordSpecifications.withFilter(filter), pageable)
                .map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public EvaluationRecordResponse get(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public EvaluationRecordResponse update(Long id, UpdateEvaluationRecordRequest request) {
        EvaluationRecord record = getEntity(id);
        if (request.hardwareProfileId() != null) {
            record.setHardwareProfile(hardwareRepository.findById(request.hardwareProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("HardwareProfile", request.hardwareProfileId())));
        }
        if (request.runtimeBackendId() != null) {
            record.setRuntimeBackend(runtimeRepository.findById(request.runtimeBackendId())
                    .orElseThrow(() -> new ResourceNotFoundException("RuntimeBackend", request.runtimeBackendId())));
        }
        if (request.benchmarkId() != null) {
            record.setBenchmark(resolveBenchmark(request.benchmarkId()));
        }
        if (request.status() != null) {
            record.setStatus(request.status());
        }
        if (request.latencyMs() != null) {
            record.setLatencyMs(request.latencyMs());
        }
        if (request.tokensPerSecond() != null) {
            record.setTokensPerSecond(request.tokensPerSecond());
        }
        if (request.memoryUsageMb() != null) {
            record.setMemoryUsageMb(request.memoryUsageMb());
        }
        if (request.score() != null) {
            record.setScore(request.score());
        }
        if (request.notes() != null) {
            record.setNotes(request.notes());
        }
        auditService.record(AuditAction.UPDATE, "EvaluationRecord", id);
        return mapper.toResponse(record);
    }

    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public void delete(Long id) {
        repository.delete(getEntity(id));
        auditService.record(AuditAction.DELETE, "EvaluationRecord", id);
    }

    @Transactional(readOnly = true)
    public List<EvaluationAggregateResponse> aggregateByModel(Long benchmarkId) {
        return repository.aggregateByModel(benchmarkId).stream()
                .map(row -> new EvaluationAggregateResponse(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2],
                        (Double) row[3],
                        (Double) row[4],
                        (Double) row[5]))
                .toList();
    }

    private BenchmarkDefinition resolveBenchmark(Long benchmarkId) {
        return benchmarkRepository.findById(benchmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("BenchmarkDefinition", benchmarkId));
    }

    private EvaluationRecord getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationRecord", id));
    }
}
