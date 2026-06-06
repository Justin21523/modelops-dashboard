package com.justin.modelops.inference;

import com.justin.modelops.common.audit.AuditService;
import com.justin.modelops.common.exception.BusinessException;
import com.justin.modelops.common.exception.ErrorCode;
import com.justin.modelops.inference.adapter.InferenceResult;
import com.justin.modelops.inference.adapter.RuntimeAdapter;
import com.justin.modelops.inference.adapter.RuntimeAdapterRegistry;
import com.justin.modelops.inference.entity.InferenceTask;
import com.justin.modelops.inference.enums.InferenceTaskStatus;
import com.justin.modelops.inference.event.InferenceTaskQueuedEvent;
import com.justin.modelops.inference.mapper.InferenceTaskMapper;
import com.justin.modelops.inference.repository.InferenceTaskRepository;
import com.justin.modelops.inference.service.InferenceTaskExecutor;
import com.justin.modelops.inference.service.InferenceTaskService;
import com.justin.modelops.model.entity.AiModel;
import com.justin.modelops.model.repository.AiModelRepository;
import com.justin.modelops.notification.TaskEventPublisher;
import com.justin.modelops.notification.dto.TaskStatusMessage;
import com.justin.modelops.runtime.enums.BackendType;
import com.justin.modelops.runtime.repository.RuntimeBackendRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InferenceTaskLifecycleTest {

    @Mock
    private InferenceTaskRepository taskRepository;
    @Mock
    private AiModelRepository modelRepository;
    @Mock
    private RuntimeBackendRepository runtimeBackendRepository;
    @Mock
    private InferenceTaskMapper mapper;
    @Mock
    private AuditService auditService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RuntimeAdapterRegistry adapterRegistry;
    @Mock
    private TaskEventPublisher taskEventPublisher;

    private InferenceTaskService service() {
        return new InferenceTaskService(taskRepository, modelRepository, runtimeBackendRepository,
                mapper, auditService, eventPublisher);
    }

    private InferenceTask queuedTask(long id) {
        AiModel model = new AiModel();
        ReflectionTestUtils.setField(model, "id", 7L);
        model.setName("Llama 3 8B");
        InferenceTask task = new InferenceTask();
        ReflectionTestUtils.setField(task, "id", id);
        task.setModel(model);
        task.setPrompt("Hello");
        task.setStatus(InferenceTaskStatus.QUEUED);
        return task;
    }

    @Test
    void markRunning_setsRunningStatusAndStartTime() {
        InferenceTaskService service = service();
        InferenceTask task = queuedTask(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskStatusMessage message = service.markRunning(1L);

        assertThat(task.getStatus()).isEqualTo(InferenceTaskStatus.RUNNING);
        assertThat(task.getStartedAt()).isNotNull();
        assertThat(message.status()).isEqualTo(InferenceTaskStatus.RUNNING);
        assertThat(message.taskId()).isEqualTo(1L);
    }

    @Test
    void markSucceeded_persistsMetrics() {
        InferenceTaskService service = service();
        InferenceTask task = queuedTask(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskStatusMessage message = service.markSucceeded(1L, new InferenceResult("done", 420L, 64.5));

        assertThat(task.getStatus()).isEqualTo(InferenceTaskStatus.SUCCEEDED);
        assertThat(task.getOutputSummary()).isEqualTo("done");
        assertThat(task.getLatencyMs()).isEqualTo(420L);
        assertThat(task.getTokensPerSecond()).isEqualTo(64.5);
        assertThat(task.getFinishedAt()).isNotNull();
        assertThat(message.latencyMs()).isEqualTo(420L);
    }

    @Test
    void markFailed_recordsErrorMessage() {
        InferenceTaskService service = service();
        InferenceTask task = queuedTask(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskStatusMessage message = service.markFailed(1L, "boom");

        assertThat(task.getStatus()).isEqualTo(InferenceTaskStatus.FAILED);
        assertThat(task.getErrorMessage()).isEqualTo("boom");
        assertThat(message.status()).isEqualTo(InferenceTaskStatus.FAILED);
    }

    @Test
    void run_rejectsNonQueuedTask() {
        InferenceTaskService service = service();
        InferenceTask task = queuedTask(1L);
        task.setStatus(InferenceTaskStatus.RUNNING);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.run(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_STATE_TRANSITION);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executor_runsQueuedToSucceeded() {
        InferenceTaskService inferenceService = mock(InferenceTaskService.class);
        InferenceTaskExecutor executor =
                new InferenceTaskExecutor(inferenceService, adapterRegistry, taskEventPublisher);

        InferenceTask task = queuedTask(1L);
        RuntimeAdapter adapter = mock(RuntimeAdapter.class);
        InferenceResult result = new InferenceResult("ok", 300L, 50.0);

        when(inferenceService.markRunning(1L))
                .thenReturn(TaskStatusMessage.of(1L, 7L, InferenceTaskStatus.RUNNING, "started"));
        when(inferenceService.loadForExecution(1L)).thenReturn(task);
        when(adapterRegistry.resolve(BackendType.MOCK)).thenReturn(adapter);
        when(adapter.run(task)).thenReturn(result);
        when(inferenceService.markSucceeded(1L, result))
                .thenReturn(TaskStatusMessage.completed(1L, 7L, InferenceTaskStatus.SUCCEEDED,
                        "done", 300L, 50.0));

        executor.onTaskQueued(new InferenceTaskQueuedEvent(1L));

        verify(inferenceService).markRunning(1L);
        verify(inferenceService).markSucceeded(1L, result);
        verify(inferenceService, never()).markFailed(eq(1L), any());

        ArgumentCaptor<TaskStatusMessage> captor = ArgumentCaptor.forClass(TaskStatusMessage.class);
        verify(taskEventPublisher, org.mockito.Mockito.times(2)).publish(captor.capture());
        assertThat(captor.getAllValues()).extracting(TaskStatusMessage::status)
                .containsExactly(InferenceTaskStatus.RUNNING, InferenceTaskStatus.SUCCEEDED);
    }

    @Test
    void executor_marksFailedWhenAdapterThrows() {
        InferenceTaskService inferenceService = mock(InferenceTaskService.class);
        InferenceTaskExecutor executor =
                new InferenceTaskExecutor(inferenceService, adapterRegistry, taskEventPublisher);

        InferenceTask task = queuedTask(1L);
        RuntimeAdapter adapter = mock(RuntimeAdapter.class);

        when(inferenceService.markRunning(1L))
                .thenReturn(TaskStatusMessage.of(1L, 7L, InferenceTaskStatus.RUNNING, "started"));
        when(inferenceService.loadForExecution(1L)).thenReturn(task);
        when(adapterRegistry.resolve(BackendType.MOCK)).thenReturn(adapter);
        when(adapter.run(task)).thenThrow(new IllegalStateException("backend down"));
        when(inferenceService.markFailed(1L, "backend down"))
                .thenReturn(TaskStatusMessage.of(1L, 7L, InferenceTaskStatus.FAILED, "failed"));

        executor.onTaskQueued(new InferenceTaskQueuedEvent(1L));

        verify(inferenceService).markFailed(1L, "backend down");
        verify(inferenceService, never()).markSucceeded(eq(1L), any());
    }
}
