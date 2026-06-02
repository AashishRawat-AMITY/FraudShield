package com.fraudshield.fraudshield.kafka;

import com.fraudshield.fraudshield.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProducer {

    // Topic names — centralized here so no magic strings elsewhere
    public static final String TRANSACTION_TOPIC = "transaction-events";
    public static final String FRAUD_ALERT_TOPIC  = "fraud-alerts";
    public static final String AUDIT_TOPIC        = "audit-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public TransactionProducer() {
        this.objectMapper = new ObjectMapper();
        // Register module to handle LocalDateTime serialization
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLISH: Transaction analyzed — goes to transaction-events topic
    // ─────────────────────────────────────────────────────────────────
    public void publishTransactionEvent(Transaction transaction) {
        try {
            // Convert Transaction object → JSON string
            String message = objectMapper.writeValueAsString(transaction);

            // Send to Kafka
            // Key = senderId → all events from same user go to same partition
            kafkaTemplate.send(
                    TRANSACTION_TOPIC,
                    transaction.getSenderId(), // key
                    message                    // value
            );

            System.out.println("[KAFKA PRODUCER] Transaction event published: "
                    + transaction.getId());

        } catch (Exception e) {
            // Kafka failure must NEVER crash the main flow
            System.err.println("[KAFKA PRODUCER] Failed to publish: "
                    + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLISH: Fraud detected — goes to fraud-alerts topic
    // ─────────────────────────────────────────────────────────────────
    public void publishFraudAlert(Transaction transaction, int riskScore) {
        try {
            // Build a lightweight alert message
            String alertMessage = String.format(
                    "{\"transactionId\":\"%s\",\"senderId\":\"%s\"," +
                            "\"amount\":%.2f,\"riskScore\":%d,\"status\":\"%s\"}",
                    transaction.getId(),
                    transaction.getSenderId(),
                    transaction.getAmount(),
                    riskScore,
                    transaction.getStatus().name()
            );

            kafkaTemplate.send(
                    FRAUD_ALERT_TOPIC,
                    transaction.getSenderId(),
                    alertMessage
            );

            System.out.println("[KAFKA PRODUCER] Fraud alert published for: "
                    + transaction.getSenderId());

        } catch (Exception e) {
            System.err.println("[KAFKA PRODUCER] Failed to publish fraud alert: "
                    + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLISH: Audit event — goes to audit-events topic
    // ─────────────────────────────────────────────────────────────────
    public void publishAuditEvent(String action, String description,
                                  String performedBy) {
        try {
            String auditMessage = String.format(
                    "{\"action\":\"%s\",\"description\":\"%s\"," +
                            "\"performedBy\":\"%s\"}",
                    action, description, performedBy
            );

            kafkaTemplate.send(
                    AUDIT_TOPIC,
                    performedBy,
                    auditMessage
            );

        } catch (Exception e) {
            System.err.println("[KAFKA PRODUCER] Failed to publish audit: "
                    + e.getMessage());
        }
    }
}