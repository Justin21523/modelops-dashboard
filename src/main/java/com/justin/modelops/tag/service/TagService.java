package com.justin.modelops.tag.service;

import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.tag.dto.CreateTagRequest;
import com.justin.modelops.tag.dto.TagResponse;
import com.justin.modelops.tag.dto.UpdateTagRequest;
import com.justin.modelops.tag.entity.Tag;
import com.justin.modelops.tag.mapper.TagMapper;
import com.justin.modelops.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper mapper;
    private final AuditService auditService;

    @Transactional
    public TagResponse create(CreateTagRequest request) {
        if (tagRepository.findByName(request.name()).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Tag already exists: " + request.name());
        }
        Tag saved = tagRepository.save(mapper.toEntity(request));
        auditService.record(AuditAction.CREATE, "Tag", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TagResponse> list(Pageable pageable) {
        return tagRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TagResponse get(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public TagResponse update(Long id, UpdateTagRequest request) {
        Tag tag = getEntity(id);
        if (request.name() != null && !request.name().equals(tag.getName())
                && tagRepository.findByName(request.name()).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Tag already exists: " + request.name());
        }
        mapper.updateEntity(request, tag);
        auditService.record(AuditAction.UPDATE, "Tag", id);
        return mapper.toResponse(tag);
    }

    @Transactional
    public void delete(Long id) {
        tagRepository.delete(getEntity(id));
        auditService.record(AuditAction.DELETE, "Tag", id);
    }

    private Tag getEntity(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }
}
