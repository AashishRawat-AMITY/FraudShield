package com.fraudshield.fraudshield.kafka;

import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.model.AuditLog;
import com.fraudshield.fraudshield.repository.TransactionRepository;
import com.fraudshield.fraudshield.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionConsumer {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper;

    public TransactionConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // ─────────────────────────────────────────────────────────────────
    // LISTEN: transaction-events topic
    // Processes every analyzed transaction in background
    // ─────────────────────────────────────────────────────────────────
    @KafkaListener(
            topics = TransactionProducer.TRANSACTION_TOPIC,
            groupId = "fraudshield-transaction-group"
    )
    public void consumeTransactionEvent(String message) {
        try {
            System.out.println("[KAFKA CONSUMER] Received transaction event");

            // Convert JSON String back to Transaction object
            Transaction transaction = objectMapper
                    .readValue(message, Transaction.class);

            // Process in background — analytics, reporting etc.
            processTransactionAsync(transaction);

        } catch (Exception e) {
            System.err.println("[KAFKA CONSUMER] Failed to process: "
                    + e.getMessage());
            // In production: send to dead letter queue for retry
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // LISTEN: fraud-alerts topic
    // Handles fraud notifications in background
    // ─────────────────────────────────────────────────────────────────
    @KafkaListener(
            topics = TransactionProducer.FRAUD_ALERT_TOPIC,
            groupId = "fraudshield-alert-group"
    )
    public void consumeFraudAlert(String message) {
        try {
            System.out.println("[KAFKA CONSUMER] Fraud alert received: "
                    + message);

            // In production:
            // Push notification to user's phone
            // Send SMS alert
            // Send email alert
            // Trigger account review workflow
            processFraudAlertAsync(message);

        } catch (Exception e) {
            System.err.println("[KAFKA CONSUMER] Failed to process alert: "
                    + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // LISTEN: audit-events topic
    // Saves audit logs asynchronously
    // ─────────────────────────────────────────────────────────────────
    @KafkaListener(
            topics = TransactionProducer.AUDIT_TOPIC,
            groupId = "fraudshield-audit-group"
    )
    public void consumeAuditEvent(String message) {
        try {
            System.out.println("[KAFKA CONSUMER] Audit event received");

            // Parse and save audit log asynchronously
            // (This offloads audit saving from the main request thread)
            AuditLog log = new AuditLog();
            log.setAction(extractField(message, "action"));
            log.setDescription(extractField(message, "description"));
            log.setPerformedBy(extractField(message, "performedBy"));
            log.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(log);

        } catch (Exception e) {
            System.err.println("[KAFKA CONSUMER] Failed to save audit: "
                    + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────
    private void processTransactionAsync(Transaction transaction) {
        // Analytics processing
        // Update user behavioral profile in Redis
        // Generate reports
        System.out.println("[CONSUMER] Processing transaction: "
                + transaction.getId()
                + " | Status: " + transaction.getStatus()
                + " | Score: " + transaction.getRiskScore());
    }

    private void processFraudAlertAsync(String alertMessage) {
        // Parse alert and send notification
        System.out.println("[CONSUMER] Processing fraud alert: "
                + alertMessage);
        // TODO: Integrate with notification service
        // notificationService.sendPushNotification(userId, message);
    }

    private String extractField(String json, String fieldName) {
        try {
            // Simple extraction for our manual JSON strings
            String search = "\"" + fieldName + "\":\"";
            int start = json.indexOf(search) + search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
}