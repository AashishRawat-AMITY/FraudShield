package com.fraudshield.fraudshield.engine;

import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.repository.BlacklistRepository;
import com.fraudshield.fraudshield.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MerchantVerifier {

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String lastReason = "";

    // ─────────────────────────────────────────────────────────────────
    // MAIN METHOD — is the receiver/merchant legitimate?
    // ─────────────────────────────────────────────────────────────────
    public int verify(Transaction transaction) {
        int score = 0;
        StringBuilder reasons = new StringBuilder();

        String receiverUpiId = transaction.getReceiverUpiId();

        // ── Check 1: Is receiver on blacklist? ────────────────────────
        // This is a SOFT check — hard check already in TransactionService
        // Phase 3. This adds points if found.
        boolean isBlacklisted = blacklistRepository
                .existsByUpiIdAndActiveTrue(receiverUpiId);

        if (isBlacklisted) {
            score += 40;
            reasons.append("Receiver UPI is on fraud blacklist. ");
        }

        // ── Check 2: Has this receiver been reported before? ──────────
        // Count how many times this UPI appeared as blocked receiver
        long timesBlocked = transactionRepository
                .findByReceiverUpiId(receiverUpiId)
                .stream()
                .filter(t -> "BLOCKED".equals(
                        t.getStatus() != null
                                ? t.getStatus().name() : ""))
                .count();

        if (timesBlocked >= 5) {
            score += 20;
            reasons.append("Receiver UPI was blocked ")
                    .append(timesBlocked)
                    .append(" times before. ");
        } else if (timesBlocked >= 2) {
            score += 10;
            reasons.append("Receiver UPI flagged ")
                    .append(timesBlocked)
                    .append(" times before. ");
        }

        // ── Check 3: Brand new UPI ID ─────────────────────────────────
        // New UPI IDs are higher risk — phishing sites use fresh UPIs
        long totalReceived = transactionRepository
                .findByReceiverUpiId(receiverUpiId).size();

        if (totalReceived == 0) {
            score += 10;
            reasons.append("Receiver UPI has never received money before. ");
        }

        this.lastReason = reasons.toString();
        return score;
    }

    public String getLastReason() { return lastReason; }
}