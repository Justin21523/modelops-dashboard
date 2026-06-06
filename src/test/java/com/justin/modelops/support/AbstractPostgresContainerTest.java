package com.justin.modelops.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests that need a real PostgreSQL instance.
 *
 * <p>Uses the Testcontainers <em>singleton</em> pattern: the container is started once in
 * a static initializer and reused by every test class for the lifetime of the JVM (it is
 * reaped by Ryuk at shutdown). This keeps the mapped port stable across classes, which is
 * required because Spring caches the datasource-backed application context between them —
 * a per-class {@code @Container} lifecycle would stop the container and leave cached
 * contexts pointing at a dead port.
 */
public abstract class AbstractPostgresContainerTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("modelops")
            .withUsername("modelops")
            .withPassword("modelops");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
