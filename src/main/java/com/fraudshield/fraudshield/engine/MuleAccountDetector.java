package com.fraudshield.fraudshield.engine;

import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MuleAccountDetector {

    @Autowired
    private TransactionRepository transactionRepository;

    private String lastReason = "";

    // ─────────────────────────────────────────────────────────────────
    // MAIN METHOD — is the RECEIVER a money mule?
    // ─────────────────────────────────────────────────────────────────
    public int detect(Transaction transaction) {
        int score = 0;
        StringBuilder reasons = new StringBuilder();

        String receiverUpiId = transaction.getReceiverUpiId();

        // ── Check 1: How many different senders today? ────────────────
        // Real mule accounts receive from MANY different people
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        long senderCount = transactionRepository
                .countByReceiverUpiIdAndCreatedAtAfter(
                        receiverUpiId, oneDayAgo
                );

        if (senderCount >= 30) {
            score += 35;
            reasons.append("Receiver got money from ")
                    .append(senderCount)
                    .append(" senders today — classic mule pattern. ");
        } else if (senderCount >= 10) {
            score += 20;
            reasons.append("Receiver got money from ")
                    .append(senderCount)
                    .append(" senders today — suspicious. ");
        }

        // ── Check 2: How many in last 1 hour? ────────────────────────
        // Rapid incoming = active mule operation
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        long hourlyCount = transactionRepository
                .countByReceiverUpiIdAndCreatedAtAfter(
                        receiverUpiId, oneHourAgo
                );

        if (hourlyCount >= 10) {
            score += 25;
            reasons.append("Receiver got ")
                    .append(hourlyCount)
                    .append(" incoming in last hour — active mule. ");
        }

        // ── Check 3: Large amount to new/unknown receiver ─────────────
        if (transaction.getAmount() > 50000 && senderCount == 0) {
            score += 15;
            reasons.append("Large amount to receiver with no history. ");
        }

        this.lastReason = reasons.toString();
        return score;
    }

    public String getLastReason() { return lastReason; }
}