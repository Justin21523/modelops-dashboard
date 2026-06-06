package com.justin.modelops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Enables Spring Data JPA auditing and resolves the current actor from the security
 * context so {@code createdBy} / {@code updatedBy} are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    private static final String SYSTEM_ACTOR = "system";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of(SYSTEM_ACTOR);
            }
            return Optional.of(authentication.getName());
        };
    }
}
