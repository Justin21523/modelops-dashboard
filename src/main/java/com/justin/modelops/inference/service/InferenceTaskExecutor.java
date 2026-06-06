package com.justin.modelops.inference.service;

import com.justin.modelops.config.AsyncConfig;
import com.justin.modelops.inference.adapter.InferenceResult;
import com.justin.modelops.inference.adapter.RuntimeAdapter;
import com.justin.modelops.inference.adapter.RuntimeAdapterRegistry;
import com.justin.modelops.inference.entity.InferenceTask;
import com.justin.modelops.inference.event.InferenceTaskQueuedEvent;
import com.justin.modelops.notification.TaskEventPublisher;
import com.justin.modelops.notification.dto.TaskStatusMessage;
import com.justin.modelops.runtime.enums.BackendType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Drives asynchronous execution of a queued inference task. Runs after the dispatching
 * transaction commits, selects the adapter for the task's backend, and persists each
 * state transition via {@link InferenceTaskService}, broadcasting every change. The mark*
 * methods return {@code null} when the task was canceled, in which case the corresponding
 * step is skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InferenceTaskExecutor {

    private final InferenceTaskService taskService;
    private final RuntimeAdapterRegistry adapterRegistry;
    private final TaskEventPublisher eventPublisher;

    @Async(AsyncConfig.INFERENCE_EXECUTOR)
    @TransactionalEventListener
    public void onTaskQueued(InferenceTaskQueuedEvent event) {
        execute(event.taskId());
    }

    /** Broadcasts a cancellation (or other ad-hoc status) once its transaction commits. */
    @TransactionalEventListener
    public void onStatusBroadcast(InferenceTaskService.TaskStatusBroadcast broadcast) {
        eventPublisher.publish(broadcast.message());
    }

    void execute(Long taskId) {
        TaskStatusMessage running = taskService.markRunning(taskId);
        if (running == null) {
            return; // task was canceled before execution started
        }
        eventPublisher.publish(running);
        try {
            InferenceTask task = taskService.loadForExecution(taskId);
            RuntimeAdapter adapter = adapterRegistry.resolve(backendTypeOf(task));
            InferenceResult result = adapter.run(task);
            publishIfPresent(taskService.markSucceeded(taskId, result));
        } catch (Exception ex) {
            log.warn("Inference task {} failed", taskId, ex);
            publishIfPresent(taskService.markFailed(taskId, ex.getMessage()));
        }
    }

    private void publishIfPresent(TaskStatusMessage message) {
        if (message != null) {
            eventPublisher.publish(message);
        }
    }

    private BackendType backendTypeOf(InferenceTask task) {
        return task.getRuntimeBackend() != null ? task.getRuntimeBackend().getBackendType() : BackendType.MOCK;
    }
}
