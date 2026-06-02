package com.fraudshield.fraudshield.engine;

import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BehavioralAnalyzer {

    @Autowired
    private TransactionRepository transactionRepository;

    private String lastReason = "";

    // ─────────────────────────────────────────────────────────────────
    // MAIN METHOD — is this transaction normal for THIS user?
    // ─────────────────────────────────────────────────────────────────
    public int analyze(Transaction transaction) {
        int score = 0;
        StringBuilder reasons = new StringBuilder();

        // ── Check 1: Amount vs user's average ────────────────────────
        double avgAmount = getUserAverageAmount(transaction.getSenderId());

        if (avgAmount > 0) {
            double ratio = transaction.getAmount() / avgAmount;

            if (ratio >= 10) {
                score += 25;
                reasons.append("Amount is ")
                        .append((int) ratio)
                        .append("x user average. ");
            } else if (ratio >= 3) {
                score += 15;
                reasons.append("Amount is ")
                        .append((int) ratio)
                        .append("x user average. ");
            }
        }

        // ── Check 2: Time of transaction ──────────────────────────────
        int hour = LocalDateTime.now().getHour();

        if (hour >= 1 && hour <= 4) {
            // 1AM-4AM — very unusual
            score += 20;
            reasons.append("Transaction at ")
                    .append(hour)
                    .append("AM — very unusual hour. ");
        } else if (hour >= 23 || hour == 0) {
            // 11PM-midnight — somewhat unusual
            score += 10;
            reasons.append("Late night transaction. ");
        }

        // ── Check 3: Transaction frequency ───────────────────────────
        // How many transactions has this user done in last 1 hour?
        long recentCount = getRecentTransactionCount(
                transaction.getSenderId()
        );

        if (recentCount >= 10) {
            score += 20;
            reasons.append("High frequency: ")
                    .append(recentCount)
                    .append(" transactions in last hour. ");
        } else if (recentCount >= 5) {
            score += 10;
            reasons.append("Elevated frequency: ")
                    .append(recentCount)
                    .append(" transactions in last hour. ");
        }

        // ── Check 4: New user (first transaction) ────────────────────
        if (avgAmount == 0) {
            // No past transactions — this is first ever
            score += 5;
            reasons.append("First transaction for this user. ");
        }

        this.lastReason = reasons.toString();
        return score;
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER: Get user's average transaction amount
    // ─────────────────────────────────────────────────────────────────
    private double getUserAverageAmount(String senderId) {
        // Get last 30 days transactions
        LocalDateTime thirtyDaysAgo =
                LocalDateTime.now().minusDays(30);

        List<Transaction> pastTransactions =
                transactionRepository.findBySenderIdAndCreatedAtBetween(
                        senderId,
                        thirtyDaysAgo,
                        LocalDateTime.now()
                );

        if (pastTransactions.isEmpty()) return 0;

        // Calculate average
        return pastTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .average()
                .orElse(0);
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPER: Count recent transactions (last 1 hour)
    // ─────────────────────────────────────────────────────────────────
    private long getRecentTransactionCount(String senderId) {
        LocalDateTime oneHourAgo =
                LocalDateTime.now().minusHours(1);

        List<Transaction> recent =
                transactionRepository.findBySenderIdAndCreatedAtBetween(
                        senderId,
                        oneHourAgo,
                        LocalDateTime.now()
                );

        return recent.size();
    }

    public String getLastReason() { return lastReason; }
}