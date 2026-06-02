package com.fraudshield.fraudshield.controller;

import com.fraudshield.fraudshield.model.AuditLog;
import com.fraudshield.fraudshield.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/audit/logs
    // All logs from last 24 hours
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getRecentLogs() {
        return ResponseEntity.ok(auditService.getRecentLogs());
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/audit/logs/user/{userId}
    // All logs for a specific user
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(
            @PathVariable String userId) {
        return ResponseEntity.ok(auditService.getLogsByUser(userId));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/audit/logs/action/{action}
    // All logs by action type
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/logs/action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(
            @PathVariable String action) {
        return ResponseEntity.ok(auditService.getLogsByAction(action));
    }
}