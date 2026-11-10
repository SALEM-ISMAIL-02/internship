package com.example.elearning.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * OBSERVER listener (SRP: write-side only).
 * Turns domain {@link AuditEvent}s into persisted audit rows.
 * Publishers stay unaware of persistence, logging and SLAs.
 * Reads live in {@link AuditQueryService}.
 */
@Slf4j
@Component
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditEvent(AuditEvent event) {
        log.info("AUDIT {} {}#{} by {}{}", event.action(), event.entityType(), event.entityId(),
                event.actor(), event.details() == null ? "" : " - " + event.details());
        auditLogRepository.save(AuditLog.builder()
                .action(event.action())
                .actor(event.actor())
                .entityType(event.entityType())
                .entityId(event.entityId())
                .details(event.details())
                .createdAt(LocalDateTime.now())
                .build());
    }
}
