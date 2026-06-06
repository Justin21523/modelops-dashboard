package com.justin.modelops.model;

import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.model.dto.CreateModelRequest;
import com.justin.modelops.model.dto.ModelResponse;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.entity.ModelFormat;
import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelModality;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import com.justin.modelops.model.mapper.ModelMapper;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.model.repository.ModelFormatRepository;
import com.justin.modelops.model.service.ModelService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelServiceTest {

    @Mock
    private AiModelRepository modelRepository;
    @Mock
    private ModelFormatRepository formatRepository;
    @Mock
    private com.justin.modelops.tag.repository.TagRepository tagRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private AuditService auditService;

    private ModelService modelService;

    @BeforeEach
    void setUp() {
        modelService = new ModelService(modelRepository, formatRepository, tagRepository, modelMapper, auditService);
    }

    @Test
    void create_resolvesFormatAndPersistsModel() {
        ModelFormat gguf = new ModelFormat();
        gguf.setType(ModelFormatType.GGUF);
        when(formatRepository.findByType(ModelFormatType.GGUF)).thenReturn(Optional.of(gguf));
        when(modelRepository.save(any(AiModel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.toResponse(any(AiModel.class)))
                .thenReturn(new ModelResponse(1L, "Llama 3 8B", null, "Meta",
                        ModelModality.TEXT, ModelFormatType.GGUF, QuantizationType.Q4_K_M,
                        "8B", 6144, null, null, "alias://models/llama3", null,
                        ModelStatus.READY, java.util.Set.of(), null, null));

        CreateModelRequest request = new CreateModelRequest("Llama 3 8B", null, "Meta",
                ModelModality.TEXT, ModelFormatType.GGUF, QuantizationType.Q4_K_M, "8B", 6144,
                null, null, "alias://models/llama3", null, ModelStatus.READY);

        ModelResponse response = modelService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        ArgumentCaptor<AiModel> captor = ArgumentCaptor.forClass(AiModel.class);
        verify(modelRepository).save(captor.capture());
        AiModel saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Llama 3 8B");
        assertThat(saved.getFormat()).isSameAs(gguf);
        assertThat(saved.getModality()).isEqualTo(ModelModality.TEXT);
        assertThat(saved.getStatus()).isEqualTo(ModelStatus.READY);
        verify(auditService).record(any(), eq("AiModel"), any());
    }

    @Test
    void create_defaultsQuantizationAndStatusWhenNull() {
        ModelFormat onnx = new ModelFormat();
        onnx.setType(ModelFormatType.ONNX);
        when(formatRepository.findByType(ModelFormatType.ONNX)).thenReturn(Optional.of(onnx));
        when(modelRepository.save(any(AiModel.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.toResponse(any(AiModel.class))).thenReturn(
                new ModelResponse(2L, "Embed", null, null, ModelModality.EMBEDDING,
                        ModelFormatType.ONNX, QuantizationType.NONE, null, null, null, null,
                        null, null, ModelStatus.DRAFT, java.util.Set.of(), null, null));

        CreateModelRequest request = new CreateModelRequest("Embed", null, null,
                ModelModality.EMBEDDING, ModelFormatType.ONNX, null, null, null, null, null,
                null, null, null);

        modelService.create(request);

        ArgumentCaptor<AiModel> captor = ArgumentCaptor.forClass(AiModel.class);
        verify(modelRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantization()).isEqualTo(QuantizationType.NONE);
        assertThat(captor.getValue().getStatus()).isEqualTo(ModelStatus.DRAFT);
    }

    @Test
    void get_throwsWhenModelMissing() {
        when(modelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> modelService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("AiModel");
    }
}
