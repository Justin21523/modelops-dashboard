package com.justin.modelops.evaluation;

import com.justin.modelops.benchmark.repository.BenchmarkDefinitionRepository;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.evaluation.dto.CreateEvaluationRecordRequest;
import com.justin.modelops.evaluation.dto.EvaluationRecordResponse;
import com.justin.modelops.evaluation.entity.EvaluationRecord;
import com.justin.modelops.evaluation.enums.EvaluationStatus;
import com.justin.modelops.evaluation.mapper.EvaluationMapper;
import com.justin.modelops.evaluation.repository.EvaluationRecordRepository;
import com.justin.modelops.evaluation.service.EvaluationRecordService;
import com.justin.modelops.hardware.repository.HardwareProfileRepository;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.runtime.repository.RuntimeBackendRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationRecordServiceTest {

    @Mock
    private EvaluationRecordRepository repository;
    @Mock
    private AiModelRepository modelRepository;
    @Mock
    private HardwareProfileRepository hardwareRepository;
    @Mock
    private RuntimeBackendRepository runtimeRepository;
    @Mock
    private BenchmarkDefinitionRepository benchmarkRepository;
    @Mock
    private EvaluationMapper mapper;
    @Mock
    private AuditService auditService;

    private EvaluationRecordService service;

    @BeforeEach
    void setUp() {
        service = new EvaluationRecordService(repository, modelRepository, hardwareRepository,
                runtimeRepository, benchmarkRepository, mapper, auditService);
    }

    @Test
    void create_resolvesModelAndDefaultsStatus() {
        AiModel model = new AiModel();
        when(modelRepository.findById(5L)).thenReturn(Optional.of(model));
        when(repository.save(any(EvaluationRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(EvaluationRecord.class)))
                .thenReturn(new EvaluationRecordResponse(1L, 5L, "m", null, null, null, null,
                        null, null, EvaluationStatus.PENDING, null, null, null, 0.9, null, null, null));

        CreateEvaluationRecordRequest request = new CreateEvaluationRecordRequest(
                5L, null, null, null, null, null, null, null, 0.9, "good");

        EvaluationRecordResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(1L);
        ArgumentCaptor<EvaluationRecord> captor = ArgumentCaptor.forClass(EvaluationRecord.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getModel()).isSameAs(model);
        assertThat(captor.getValue().getStatus()).isEqualTo(EvaluationStatus.PENDING);
        assertThat(captor.getValue().getScore()).isEqualTo(0.9);
        verify(auditService).record(any(), eq("EvaluationRecord"), any());
    }

    @Test
    void create_throwsWhenModelMissing() {
        when(modelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                new CreateEvaluationRecordRequest(99L, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("AiModel");

        verify(repository, never()).save(any());
    }
}
