package com.justin.modelops.inference.service;

import com.justin.modelops.common.audit.AuditAction;
import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.common.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Manages the inference task lifecycle. The state-transition methods are individually
 * transactional so each step (QUEUED -> RUNNING -> terminal) is persisted and visible,
 * and are invoked by {@code InferenceTaskExecutor} on a background thread.
 */
@Service
@RequiredArgsConstructor
public class InferenceTaskService {

    private final InferenceTaskRepository taskRepository;
    private final AiModelRepository modelRepository;
    private final RuntimeBackendRepository runtimeBackendRepository;
    private final InferenceTaskMapper mapper;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

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
        task.setParameters(request.parameters());
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

    @Transactional(readOnly = true)
    public InferenceTask loadForExecution(Long id) {
        return getEntity(id);
    }

    @Transactional
    public TaskStatusMessage markRunning(Long id) {
        InferenceTask task = getEntity(id);
        task.setStatus(InferenceTaskStatus.RUNNING);
        task.setStartedAt(Instant.now());
        return TaskStatusMessage.of(task.getId(), task.getModel().getId(),
                InferenceTaskStatus.RUNNING, "Inference started");
    }

    @Transactional
    public TaskStatusMessage markSucceeded(Long id, InferenceResult result) {
        InferenceTask task = getEntity(id);
        task.setStatus(InferenceTaskStatus.SUCCEEDED);
        task.setOutputSummary(result.outputSummary());
        task.setLatencyMs(result.latencyMs());
        task.setTokensPerSecond(result.tokensPerSecond());
        task.setFinishedAt(Instant.now());
        return TaskStatusMessage.completed(task.getId(), task.getModel().getId(),
                InferenceTaskStatus.SUCCEEDED, "Inference completed",
                result.latencyMs(), result.tokensPerSecond());
    }

    @Transactional
    public TaskStatusMessage markFailed(Long id, String errorMessage) {
        InferenceTask task = getEntity(id);
        task.setStatus(InferenceTaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        task.setFinishedAt(Instant.now());
        return TaskStatusMessage.of(task.getId(), task.getModel().getId(),
                InferenceTaskStatus.FAILED, "Inference failed: " + errorMessage);
    }

    private InferenceTask getEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InferenceTask", id));
    }
}
