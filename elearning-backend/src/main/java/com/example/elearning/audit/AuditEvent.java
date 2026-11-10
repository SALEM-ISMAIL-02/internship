package com.example.elearning.audit;

/**
 * OBSERVER: domain fact published through Spring's ApplicationEventPublisher.
 * Any number of listeners may react without the publisher knowing about them.
 */
public record AuditEvent(String action, String actor, String entityType, Long entityId, String details) {

    public AuditEvent(String action, String actor, String entityType, Long entityId) {
        this(action, actor, entityType, entityId, null);
    }
}
