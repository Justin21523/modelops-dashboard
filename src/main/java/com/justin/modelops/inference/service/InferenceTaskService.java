package com.justin.modelops.inference.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.common.exception.ResourceNotFoundException;
import com.justin.modelops.config.RedisConfig;
import com.justin.modelops.inference.adapter.InferenceResult;
import com.justin.modelops.inference.dto.CreateInferenceTaskRequest;
import com.justin.modelops.inference.dto.InferenceTaskResponse;
import com.justin.modelops.inference.entity.InferenceTask;
import com.justin.modelops.inference.enums.InferenceTaskStatus;
import com.justin.modelops.inference.event.InferenceTaskQueuedEvent;
import com.justin.modelops.inference.mapper.InferenceTaskMapper;
import com.justin.modelops.inference.repository.InferenceTaskRepository;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.notification.dto.TaskStatusMessage;
import com.justin.modelops.runtime.entity.RuntimeBackend;
import com.justin.modelops.runtime.repository.RuntimeBackendRepository;
import com.justin.modelops.tag.entity.Tag;
import com.justin.modelops.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Manages the inference task lifecycle. The state-transition methods are individually
 * transactional and guarded by the current status so they compose safely with
 * cancellation; they are invoked by {@code InferenceTaskExecutor} on a background thread.
 */
@Service
@RequiredArgsConstructor
public class InferenceTaskService {

    private final InferenceTaskRepository taskRepository;
    private final AiModelRepository modelRepository;
    private final RuntimeBackendRepository runtimeBackendRepository;
    private final TagRepository tagRepository;
    private final InferenceTaskMapper mapper;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public InferenceTaskResponse create(CreateInferenceTaskRequest request) {
        AiModel model = modelRepository.findById(request.modelId())
                .orElseThrow(() -> new ResourceNotFoundException("AiModel", request.modelId()));

        InferenceTask task = new InferenceTask();
        task.setModel(model);
        if (request.runtimeBackendId() != null) {
            RuntimeBackend backend = runtimeBackendRepository.findById(request.runtimeBackendId())
                    .orElseThrow(() -> new ResourceNotFoundException("RuntimeBackend", request.runtimeBackendId()));
            task.setRuntimeBackend(backend);
        }
        task.setPrompt(request.prompt());
        task.setParameters(serializeParameters(request));
        task.setStatus(InferenceTaskStatus.QUEUED);

        InferenceTask saved = taskRepository.save(task);
        auditService.record(AuditAction.CREATE, "InferenceTask", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<InferenceTaskResponse> list(InferenceTaskStatus status, Pageable pageable) {
        Page<InferenceTask> page = (status == null)
                ? taskRepository.findAll(pageable)
                : taskRepository.findByStatus(status, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InferenceTaskResponse get(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    /**
     * Validates that the task is runnable and dispatches it for asynchronous execution.
     * Returns the task in its current (QUEUED) state; clients observe progress via
     * polling or the WebSocket feed.
     */
    @Transactional
    public InferenceTaskResponse run(Long id) {
        InferenceTask task = getEntity(id);
        if (task.getStatus() != InferenceTaskStatus.QUEUED) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Task %d cannot be run from status %s".formatted(id, task.getStatus()));
        }
        auditService.record(AuditAction.RUN, "InferenceTask", id);
        eventPublisher.publishEvent(new InferenceTaskQueuedEvent(id));
        return mapper.toResponse(task);
    }

    /**
     * Cooperatively cancels a task. A QUEUED task is canceled before execution; a RUNNING
     * task is flagged CANCELED so the executor's terminal transition is skipped.
     */
    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public InferenceTaskResponse cancel(Long id) {
        InferenceTask task = getEntity(id);
        if (task.getStatus().isTerminal()) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION,
                    "Task %d is already %s".formatted(id, task.getStatus()));
        }
        task.setStatus(InferenceTaskStatus.CANCELED);
        task.setFinishedAt(Instant.now());
        auditService.record(AuditAction.CANCEL, "InferenceTask", id);
        eventPublisher.publishEvent(new TaskStatusBroadcast(
                TaskStatusMessage.of(task.getId(), task.getModel().getId(),
                        InferenceTaskStatus.CANCELED, "Inference canceled")));
        return mapper.toResponse(task);
    }

    @Transactional
    public InferenceTaskResponse attachTag(Long id, Long tagId) {
        InferenceTask task = getEntity(id);
        task.addTag(resolveTag(tagId));
        auditService.record(AuditAction.UPDATE, "InferenceTask", id, "attach tag " + tagId);
        return mapper.toResponse(task);
    }

    @Transactional
    public InferenceTaskResponse detachTag(Long id, Long tagId) {
        InferenceTask task = getEntity(id);
        task.removeTag(resolveTag(tagId));
        auditService.record(AuditAction.UPDATE, "InferenceTask", id, "detach tag " + tagId);
        return mapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public InferenceTask loadForExecution(Long id) {
        return getEntity(id);
    }

    /**
     * @return a RUNNING status message, or {@code null} if the task is no longer QUEUED
     *         (e.g. it was canceled before execution started)
     */
    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public TaskStatusMessage markRunning(Long id) {
        InferenceTask task = getEntity(id);
        if (task.getStatus() != InferenceTaskStatus.QUEUED) {
            return null;
        }
        task.setStatus(InferenceTaskStatus.RUNNING);
        task.setStartedAt(Instant.now());
        return TaskStatusMessage.of(task.getId(), task.getModel().getId(),
                InferenceTaskStatus.RUNNING, "Inference started");
    }

    /**
     * @return a SUCCEEDED status message, or {@code null} if the task is no longer RUNNING
     *         (e.g. it was canceled mid-flight)
     */
    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public TaskStatusMessage markSucceeded(Long id, InferenceResult result) {
        InferenceTask task = getEntity(id);
        if (task.getStatus() != InferenceTaskStatus.RUNNING) {
            return null;
        }
        task.setStatus(InferenceTaskStatus.SUCCEEDED);
        task.setOutputSummary(result.outputSummary());
        task.setLatencyMs(result.latencyMs());
        task.setTokensPerSecond(result.tokensPerSecond());
        task.setFinishedAt(Instant.now());
        return TaskStatusMessage.completed(task.getId(), task.getModel().getId(),
                InferenceTaskStatus.SUCCEEDED, "Inference completed",
                result.latencyMs(), result.tokensPerSecond());
    }

    /**
     * @return a FAILED status message, or {@code null} if the task is no longer RUNNING
     */
    @CacheEvict(cacheNames = RedisConfig.DASHBOARD_CACHE, allEntries = true)
    @Transactional
    public TaskStatusMessage markFailed(Long id, String errorMessage) {
        InferenceTask task = getEntity(id);
        if (task.getStatus() != InferenceTaskStatus.RUNNING) {
            return null;
        }
        task.setStatus(InferenceTaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        task.setFinishedAt(Instant.now());
        return TaskStatusMessage.of(task.getId(), task.getModel().getId(),
                InferenceTaskStatus.FAILED, "Inference failed: " + errorMessage);
    }

    private String serializeParameters(CreateInferenceTaskRequest request) {
        if (request.parameters() == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(request.parameters());
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Invalid inference parameters");
        }
    }

    private Tag resolveTag(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));
    }

    private InferenceTask getEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InferenceTask", id));
    }

    /** Internal event used to broadcast a cancellation after the transaction commits. */
    public record TaskStatusBroadcast(TaskStatusMessage message) {
    }
}
