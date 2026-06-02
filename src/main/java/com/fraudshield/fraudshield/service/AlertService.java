package com.fraudshield.fraudshield.service;

import com.fraudshield.fraudshield.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    // ─────────────────────────────────────────────────────────────────
    // ALERT: Fraud detected — notify the user
    // ─────────────────────────────────────────────────────────────────
    public void sendFraudAlert(Transaction transaction, int riskScore) {

        String message = buildFraudAlertMessage(transaction, riskScore);

        // Right now: log to console
        // Later: this plugs into Kafka → push notification / email / SMS
        System.out.println("[FRAUD ALERT] " + message);

        // TODO (Kafka step):
        // kafkaProducer.send("fraud-alerts", transaction.getSenderId(), message);
    }

    // ─────────────────────────────────────────────────────────────────
    // ALERT: Suspicious transaction — flag but don't block
    // ─────────────────────────────────────────────────────────────────
    public void sendSuspiciousAlert(Transaction transaction, int riskScore) {

        String message = "Suspicious transaction detected. " +
                "Amount: ₹" + transaction.getAmount() +
                " | Risk Score: " + riskScore +
                " | Please verify if this was you.";

        System.out.println("[SUSPICIOUS ALERT] " + message);

        // TODO (Kafka step):
        // kafkaProducer.send("fraud-alerts", transaction.getSenderId(), message);
    }

    // ─────────────────────────────────────────────────────────────────
    // ALERT: New device login
    // ─────────────────────────────────────────────────────────────────
    public void sendNewDeviceAlert(String userId, String deviceId) {

        String message = "New device login detected for user: " + userId +
                " | Device: " + deviceId +
                " | If this was not you, change your password immediately.";

        System.out.println("[DEVICE ALERT] " + message);
    }

    // ─────────────────────────────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────────────────────────────
    private String buildFraudAlertMessage(Transaction transaction, int riskScore) {
        return "Transaction BLOCKED for your protection. " +
                "Amount: ₹" + transaction.getAmount() +
                " | To: " + transaction.getReceiverUpiId() +
                " | Risk Score: " + riskScore +
                " | Time: " + transaction.getCreatedAt();
    }
}