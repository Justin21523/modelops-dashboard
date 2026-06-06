package com.justin.modelops.benchmark;

import com.justin.modelops.benchmark.dto.BenchmarkResponse;
import com.justin.modelops.benchmark.dto.CreateBenchmarkRequest;
import com.justin.modelops.benchmark.entity.BenchmarkDefinition;
import com.justin.modelops.benchmark.enums.BenchmarkType;
import com.justin.modelops.benchmark.mapper.BenchmarkMapper;
import com.justin.modelops.benchmark.repository.BenchmarkDefinitionRepository;
import com.justin.modelops.benchmark.service.BenchmarkService;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.tag.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BenchmarkServiceTest {

    @Mock
    private BenchmarkDefinitionRepository repository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private BenchmarkMapper mapper;
    @Mock
    private AuditService auditService;

    private BenchmarkService service;

    @BeforeEach
    void setUp() {
        service = new BenchmarkService(repository, tagRepository, mapper, auditService);
    }

    @Test
    void create_persistsWhenNameIsUnique() {
        CreateBenchmarkRequest request = new CreateBenchmarkRequest("Coding", BenchmarkType.CODING, "desc", null);
        BenchmarkDefinition entity = new BenchmarkDefinition();
        when(repository.existsByName("Coding")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(new BenchmarkResponse(1L, "Coding",
                BenchmarkType.CODING, "desc", null, Set.of(), null, null));

        BenchmarkResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(1L);
        verify(repository).save(entity);
        verify(auditService).record(any(), eq("BenchmarkDefinition"), any());
    }

    @Test
    void create_rejectsDuplicateName() {
        when(repository.existsByName("Coding")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new CreateBenchmarkRequest("Coding", BenchmarkType.CODING, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_CONFLICT);

        verify(repository, never()).save(any());
    }

    @Test
    void get_throwsWhenMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BenchmarkDefinition");
    }
}
