package com.fraudshield.fraudshield.engine;

import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.repository.BlacklistRepository;
import com.fraudshield.fraudshield.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeviceFingerprinter {

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String lastReason = "";

    // ─────────────────────────────────────────────────────────────────
    // MAIN METHOD — is this device trusted?
    // ─────────────────────────────────────────────────────────────────
    public int fingerprint(Transaction transaction) {
        int score = 0;
        StringBuilder reasons = new StringBuilder();

        // ── Check 1: No device ID at all ─────────────────────────────
        if (transaction.getDeviceId() == null
                || transaction.getDeviceId().isBlank()) {
            score += 15;
            reasons.append("No device ID provided — suspicious. ");
            this.lastReason = reasons.toString();
            return score; // no point checking further without device
        }

        // ── Check 2: Is this device blacklisted? ─────────────────────
        boolean deviceBlacklisted = blacklistRepository
                .findByDeviceFingerprint(transaction.getDeviceId())
                .isPresent();

        if (deviceBlacklisted) {
            score += 40;
            reasons.append("Device is on fraud blacklist. ");
        }

        // ── Check 3: Has this user used this device before? ───────────
        boolean deviceKnown = hasUserUsedThisDevice(
                transaction.getSenderId(),
                transaction.getDeviceId()
        );

        if (!deviceKnown) {
            score += 20;
            reasons.append("New device never seen for this user. ");
        }

        this.lastReason = reasons.toString();
        return score;
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER: Has this user transacted from this device before?
    // ─────────────────────────────────────────────────────────────────
    private boolean hasUserUsedThisDevice(
            String senderId, String deviceId) {

        // Look at last 90 days of transactions for this user
        LocalDateTime ninetyDaysAgo =
                LocalDateTime.now().minusDays(90);

        List<Transaction> pastTransactions =
                transactionRepository.findBySenderIdAndCreatedAtBetween(
                        senderId,
                        ninetyDaysAgo,
                        LocalDateTime.now()
                );

        // Check if any past transaction used this device
        return pastTransactions.stream()
                .anyMatch(t -> deviceId.equals(t.getDeviceId()));
    }

    public String getLastReason() { return lastReason; }
}