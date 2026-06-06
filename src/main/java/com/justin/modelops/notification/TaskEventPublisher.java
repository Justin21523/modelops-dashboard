package com.justin.modelops.notification;

import com.justin.modelops.notification.dto.TaskStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Broadcasts inference task status changes to WebSocket subscribers. Publishes to a
 * shared {@code /topic/tasks} feed and a per-task {@code /topic/tasks/{id}} feed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private static final String TASKS_TOPIC = "/topic/tasks";

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(TaskStatusMessage message) {
        log.debug("Publishing task status: task={} status={}", message.taskId(), message.status());
        messagingTemplate.convertAndSend(TASKS_TOPIC, message);
        messagingTemplate.convertAndSend(TASKS_TOPIC + "/" + message.taskId(), message);
    }
}
