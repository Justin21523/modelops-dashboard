package com.justin.modelops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the ModelOps Dashboard backend. Cross-cutting features
 * ({@code @EnableCaching}, {@code @EnableAsync}) live on dedicated configuration classes
 * so test slices (e.g. {@code @DataJpaTest}) are not forced to provide their infra.
 */
@SpringBootApplication
public class ModelOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelOpsApplication.class, args);
    }
}
