package com.justin.modelops.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records audit log entries for important actions. The actor is resolved from the
 * security context so callers only describe what happened, not who did it.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String SYSTEM_ACTOR = "system";

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(AuditAction action, String entityType, Object entityId, String detail) {
        String entityIdValue = entityId == null ? null : String.valueOf(entityId);
        auditLogRepository.save(new AuditLog(action, entityType, entityIdValue, currentActor(), detail));
    }

    public void record(AuditAction action, String entityType, Object entityId) {
        record(action, entityType, entityId, null);
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return SYSTEM_ACTOR;
        }
        return authentication.getName();
    }
}
