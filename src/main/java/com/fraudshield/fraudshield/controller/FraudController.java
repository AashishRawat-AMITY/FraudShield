package com.fraudshield.fraudshield.controller;

import com.fraudshield.fraudshield.model.BlacklistEntry;
import com.fraudshield.fraudshield.repository.BlacklistRepository;
import com.fraudshield.fraudshield.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudController {

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private AuditService auditService;

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/fraud/blacklist
    // Add a UPI / device / IP to the shared blacklist
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/blacklist")
    public ResponseEntity<BlacklistEntry> addToBlacklist(
            @RequestBody BlacklistEntry entry) {

        entry.setActive(true);
        entry.setAddedAt(LocalDateTime.now());

        BlacklistEntry saved = blacklistRepository.save(entry);

        auditService.log(
                "BLACKLIST_ENTRY_ADDED",
                "Added to blacklist: " + entry.getUpiId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    // ─────────────────────────────────────────────────────────────────
    // DELETE /api/v1/fraud/blacklist/{upiId}
    // Deactivate a blacklist entry (soft delete)
    // ─────────────────────────────────────────────────────────────────
    @DeleteMapping("/blacklist/{upiId}")
    public ResponseEntity<Map<String, String>> removeFromBlacklist(
            @PathVariable String upiId) {

        blacklistRepository.findByUpiId(upiId).ifPresent(entry -> {
            entry.setActive(false);      // soft delete — keep record
            blacklistRepository.save(entry);

            auditService.log(
                    "BLACKLIST_ENTRY_REMOVED",
                    "Removed from blacklist: " + upiId,
                    entry.getId()
            );
        });

        return ResponseEntity.ok(
                Map.of("message", "Blacklist entry deactivated: " + upiId)
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/fraud/blacklist
    // View all active blacklist entries
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/blacklist")
    public ResponseEntity<List<BlacklistEntry>> getBlacklist() {

        List<BlacklistEntry> entries =
                blacklistRepository.findByActiveTrue();
        return ResponseEntity.ok(entries);
    }
}