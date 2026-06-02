package com.fraudshield.fraudshield.engine;

import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.repository.BlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RiskScoringEngine {

    @Autowired
    private BehavioralAnalyzer behavioralAnalyzer;

    @Autowired
    private MuleAccountDetector muleAccountDetector;

    @Autowired
    private DeviceFingerprinter deviceFingerprinter;

    @Autowired
    private MerchantVerifier merchantVerifier;

    @Autowired
    private BlacklistRepository blacklistRepository;

    // ─────────────────────────────────────────────────────────────────
    // MAIN METHOD — called by TransactionService
    // Runs all 5 analyzers, combines scores, caps at 100
    // ─────────────────────────────────────────────────────────────────
    public EngineResult evaluate(Transaction transaction) {

        EngineResult result = new EngineResult();

        // ── Layer 1: Behavioral Analysis ─────────────────────────────
        int behavioralScore = behavioralAnalyzer.analyze(transaction);
        result.setBehavioralScore(behavioralScore);
        result.addReason(behavioralAnalyzer.getLastReason());

        // ── Layer 2: Mule Account Detection ──────────────────────────
        int muleScore = muleAccountDetector.detect(transaction);
        result.setMuleScore(muleScore);
        result.addReason(muleAccountDetector.getLastReason());

        // ── Layer 3: Device Fingerprinting ────────────────────────────
        int deviceScore = deviceFingerprinter.fingerprint(transaction);
        result.setDeviceScore(deviceScore);
        result.addReason(deviceFingerprinter.getLastReason());

        // ── Layer 4: Merchant Verification ───────────────────────────
        int merchantScore = merchantVerifier.verify(transaction);
        result.setMerchantScore(merchantScore);
        result.addReason(merchantVerifier.getLastReason());

        // ── Layer 5: IP Risk Check ────────────────────────────────────
        int ipScore = checkIpRisk(transaction);
        result.setIpScore(ipScore);

        // ── Layer 6: Final Score Calculation ─────────────────────────
        int totalScore = behavioralScore + muleScore
                + deviceScore + merchantScore + ipScore;

        // Cap at 100 — score cannot exceed 100
        int finalScore = Math.min(totalScore, 100);
        result.setFinalScore(finalScore);

        return result;
    }

    // ── Layer 5: IP Risk ─────────────────────────────────────────────
    private int checkIpRisk(Transaction transaction) {
        if (transaction.getIpAddress() == null
                || transaction.getIpAddress().isBlank()) {
            return 10; // no IP provided — slightly suspicious
        }

        // Check if IP is on blacklist
        if (blacklistRepository
                .findByIpAddress(transaction.getIpAddress()).isPresent()) {
            return 40; // blacklisted IP — very suspicious
        }

        return 0;
    }

    // ─────────────────────────────────────────────────────────────────
    // EngineResult — carries all layer scores + reasons
    // ─────────────────────────────────────────────────────────────────
    public static class EngineResult {
        private int behavioralScore;
        private int muleScore;
        private int deviceScore;
        private int merchantScore;
        private int ipScore;
        private int finalScore;
        private StringBuilder reasons = new StringBuilder();

        public void addReason(String reason) {
            if (reason != null && !reason.isBlank()) {
                if (reasons.length() > 0) reasons.append(" | ");
                reasons.append(reason);
            }
        }

        public String getReasons() { return reasons.toString(); }

        // Getters and Setters
        public int getBehavioralScore() { return behavioralScore; }
        public void setBehavioralScore(int s) { this.behavioralScore = s; }
        public int getMuleScore() { return muleScore; }
        public void setMuleScore(int s) { this.muleScore = s; }
        public int getDeviceScore() { return deviceScore; }
        public void setDeviceScore(int s) { this.deviceScore = s; }
        public int getMerchantScore() { return merchantScore; }
        public void setMerchantScore(int s) { this.merchantScore = s; }
        public int getIpScore() { return ipScore; }
        public void setIpScore(int s) { this.ipScore = s; }
        public int getFinalScore() { return finalScore; }
        public void setFinalScore(int s) { this.finalScore = s; }
    }
}