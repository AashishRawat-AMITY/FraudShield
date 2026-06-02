package com.fraudshield.fraudshield.repository;



import com.fraudshield.fraudshield.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // Get all audit events for a specific user
    List<AuditLog> findByUserId(String userId);

    // Get recent audit logs (last 24h monitoring)
    List<AuditLog> findByCreatedAtAfter(LocalDateTime after);

    // Get audit logs by action type (e.g., "LOGIN", "TRANSACTION_BLOCKED")
    List<AuditLog> findByAction(String action);
}