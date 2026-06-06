package com.justin.modelops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Dedicated executor for asynchronous inference task execution so the
 * {@code /run} endpoint returns immediately while the mock adapter works.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String INFERENCE_EXECUTOR = "inferenceExecutor";

    @Bean(name = INFERENCE_EXECUTOR)
    public Executor inferenceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("inference-");
        executor.initialize();
        return executor;
    }
}
