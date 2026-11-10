package com.example.elearning.controller;

import com.example.elearning.audit.AuditLog;
import com.example.elearning.audit.AuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-only compliance trail (SecurityConfig restricts /api/admin/** to ROLE_ADMIN). */
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryService auditQueryService;

    @GetMapping
    public List<AuditLog> recent() {
        return auditQueryService.recent();
    }

    @GetMapping("/{entityType}/{entityId}")
    public List<AuditLog> forEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return auditQueryService.forEntity(entityType, entityId);
    }
}
