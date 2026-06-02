package com.fraudshield.fraudshield.service;

import com.fraudshield.fraudshield.model.AuditLog;
import com.fraudshield.fraudshield.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // ─────────────────────────────────────────────────────────────────
    // CORE METHOD — called by every other service after key actions
    // ─────────────────────────────────────────────────────────────────
    public void log(String action, String description, String targetId) {

        // Step 1: Who is making this request?
        // Reads from SecurityContextHolder — set by JwtFilter earlier
        String performedBy = "SYSTEM"; // default for non-HTTP triggered logs
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                performedBy = auth.getName(); // → "hdfc@hdfc.com"
            }
        } catch (Exception e) {
            // silent — audit should never crash the main flow
        }

        // Step 2: Build the log entry
        AuditLog log = new AuditLog();
        log.setAction(action);           // e.g. "TRANSACTION_BLOCKED"
        log.setDescription(description); // e.g. "Risk score 95 — mule account"
        log.setTargetId(targetId);       // e.g. transaction ID or user ID
        log.setPerformedBy(performedBy); // e.g. "hdfc@hdfc.com"
        log.setCreatedAt(LocalDateTime.now());

        // Step 3: Save to MongoDB (fire and don't block)
        auditLogRepository.save(log);
    }

    // ─────────────────────────────────────────────────────────────────
    // OVERLOAD — when no targetId needed
    // ─────────────────────────────────────────────────────────────────
    public void log(String action, String description) {
        log(action, description, null);
    }

    // ─────────────────────────────────────────────────────────────────
    // READ METHODS — used by AuditController
    // ─────────────────────────────────────────────────────────────────
    public List<AuditLog> getLogsByUser(String userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getRecentLogs() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        return auditLogRepository.findByCreatedAtAfter(last24Hours);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
}