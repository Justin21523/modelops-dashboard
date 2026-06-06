package com.justin.modelops.model.service;

import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.model.dto.CreateModelRequest;
import com.justin.modelops.model.dto.ModelFilter;
import com.justin.modelops.model.dto.ModelResponse;
import com.justin.modelops.model.dto.UpdateModelRequest;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.entity.ModelFormat;
import com.justin.modelops.model.enums.ModelFormatType;
import com.justin.modelops.model.enums.ModelStatus;
import com.justin.modelops.model.enums.QuantizationType;
import com.justin.modelops.model.mapper.ModelMapper;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.model.repository.AiModelSpecifications;
import com.justin.modelops.model.repository.ModelFormatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final AiModelRepository modelRepository;
    private final ModelFormatRepository formatRepository;
    private final ModelMapper modelMapper;
    private final AuditService auditService;

    @Transactional
    public ModelResponse create(CreateModelRequest request) {
        AiModel model = new AiModel();
        model.setName(request.name());
        model.setFamily(request.family());
        model.setProvider(request.provider());
        model.setModality(request.modality());
        model.setFormat(resolveFormat(request.formatType()));
        model.setQuantization(request.quantization() == null ? QuantizationType.NONE : request.quantization());
        model.setParameterSize(request.parameterSize());
        model.setEstimatedVramMb(request.estimatedVramMb());
        model.setLicense(request.license());
        model.setSourceUrl(request.sourceUrl());
        model.setStorageNote(request.storageNote());
        model.setNotes(request.notes());
        model.setStatus(request.status() == null ? ModelStatus.DRAFT : request.status());

        AiModel saved = modelRepository.save(model);
        auditService.record(AuditAction.CREATE, "AiModel", saved.getId());
        return modelMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ModelResponse> list(ModelFilter filter, Pageable pageable) {
        return modelRepository.findAll(AiModelSpecifications.withFilter(filter), pageable)
                .map(modelMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ModelResponse get(Long id) {
        return modelMapper.toResponse(getEntity(id));
    }

    @Transactional
    public ModelResponse update(Long id, UpdateModelRequest request) {
        AiModel model = getEntity(id);
        modelMapper.updateEntity(request, model);
        if (request.formatType() != null) {
            model.setFormat(resolveFormat(request.formatType()));
        }
        auditService.record(AuditAction.UPDATE, "AiModel", id);
        return modelMapper.toResponse(model);
    }

    @Transactional
    public void delete(Long id) {
        AiModel model = getEntity(id);
        modelRepository.delete(model);
        auditService.record(AuditAction.DELETE, "AiModel", id);
    }

    private AiModel getEntity(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AiModel", id));
    }

    private ModelFormat resolveFormat(ModelFormatType type) {
        return formatRepository.findByType(type)
                .orElseThrow(() -> new ResourceNotFoundException("ModelFormat", type));
    }
}
