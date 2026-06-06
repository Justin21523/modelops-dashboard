package com.justin.modelops.inference.adapter;

import com.justin.modelops.inference.entity.InferenceTask;
import com.justin.modelops.runtime.enums.BackendType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulated runtime that fabricates a plausible result after a short delay. Used as
 * the default Phase 1 backend and as the fallback when no real adapter is available.
 *
 * <p>TODO(phase-2): replace with real adapters and remove the fallback behaviour.
 */
@Slf4j
@Component
public class MockRuntimeAdapter implements RuntimeAdapter {

    private static final int MAX_PREVIEW_CHARS = 120;

    @Override
    public boolean supports(BackendType backendType) {
        return backendType == BackendType.MOCK;
    }

    @Override
    public InferenceResult run(InferenceTask task) {
        long latencyMs = ThreadLocalRandom.current().nextLong(200, 1500);
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Mock inference interrupted", ex);
        }

        double tokensPerSecond = ThreadLocalRandom.current().nextDouble(15.0, 95.0);
        String preview = task.getPrompt().length() > MAX_PREVIEW_CHARS
                ? task.getPrompt().substring(0, MAX_PREVIEW_CHARS) + "..."
                : task.getPrompt();
        String summary = "[mock] Simulated response for model '%s' to prompt: %s"
                .formatted(task.getModel().getName(), preview);

        log.debug("Mock inference complete: task={} latencyMs={} tps={}", task.getId(), latencyMs, tokensPerSecond);
        return new InferenceResult(summary, latencyMs, tokensPerSecond);
    }
}
