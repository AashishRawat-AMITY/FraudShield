package com.fraudshield.fraudshield.service;

import com.fraudshield.fraudshield.dto.TransactionRequest;
import com.fraudshield.fraudshield.dto.TransactionResponse;
import com.fraudshield.fraudshield.engine.RiskScoringEngine;
import com.fraudshield.fraudshield.enums.RiskLevel;
import com.fraudshield.fraudshield.enums.TransactionStatus;
import com.fraudshield.fraudshield.exception.FraudDetectedException;
import com.fraudshield.fraudshield.exception.InvalidTransactionException;
import com.fraudshield.fraudshield.kafka.TransactionProducer;   // ← ADD THIS IMPORT
import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.repository.BlacklistRepository;
import com.fraudshield.fraudshield.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private RiskScoringEngine riskScoringEngine;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private TransactionProducer transactionProducer;   // ← ADD 1: new field

    // ─────────────────────────────────────────────────────────────────
    // ANALYZE — main entry point for every transaction
    // ─────────────────────────────────────────────────────────────────
    public TransactionResponse analyzeTransaction(TransactionRequest request) {

        // ── PHASE 1: Validate request data ───────────────────────────
        validateRequest(request);

        // ── PHASE 2: Build Transaction object ────────────────────────
        Transaction transaction = buildTransaction(request);

        // ── PHASE 3: Blacklist pre-check ─────────────────────────────
        if (blacklistRepository.existsByUpiIdAndActiveTrue(
                request.getReceiverUpiId())) {

            transaction.setStatus(TransactionStatus.BLOCKED);
            transaction.setRiskScore(100);
            transaction.setRiskLevel(RiskLevel.BLOCK);
            transactionRepository.save(transaction);

            auditService.log(
                    "TRANSACTION_BLOCKED_BLACKLIST",
                    "Receiver UPI on blacklist: " + request.getReceiverUpiId(),
                    transaction.getId()
            );

            throw new FraudDetectedException(100, "CRITICAL");
        }

        // ── PHASE 4: Run the full fraud engine ────────────────────────
        RiskScoringEngine.EngineResult engineResult =
                riskScoringEngine.evaluate(transaction);

        int riskScore            = engineResult.getFinalScore();
        RiskLevel riskLevel      = determineRiskLevel(riskScore);
        TransactionStatus status = determineStatus(riskScore);

        // ── PHASE 5: Save transaction with results ────────────────────
        transaction.setRiskScore(riskScore);
        transaction.setRiskLevel(riskLevel);
        transaction.setStatus(status);
        transactionRepository.save(transaction);

        // ← ADD 2: publish to Kafka after every save
        transactionProducer.publishTransactionEvent(transaction);

        // ── PHASE 6: Post-decision actions ────────────────────────────
        if (status == TransactionStatus.BLOCKED) {

            auditService.log(
                    "TRANSACTION_BLOCKED",
                    "Score: " + riskScore
                            + " | Receiver: " + request.getReceiverUpiId()
                            + " | Reasons: " + engineResult.getReasons(),
                    transaction.getId()
            );
            alertService.sendFraudAlert(transaction, riskScore);

            // ← ADD 3: publish fraud alert to Kafka on BLOCKED only
            transactionProducer.publishFraudAlert(transaction, riskScore);

            throw new FraudDetectedException(riskScore, riskLevel.name());

        } else if (status == TransactionStatus.FLAGGED) {

            auditService.log(
                    "TRANSACTION_FLAGGED",
                    "Score: " + riskScore
                            + " | Reasons: " + engineResult.getReasons(),
                    transaction.getId()
            );
            alertService.sendSuspiciousAlert(transaction, riskScore);
        }

        auditService.log(
                "TRANSACTION_APPROVED",
                "Score: " + riskScore
                        + " | Amount: " + request.getAmount(),
                transaction.getId()
        );

        // ── PHASE 7: Return result to bank ────────────────────────────
        return new TransactionResponse(
                transaction.getId(),
                status,
                riskLevel,
                riskScore,
                "Transaction analyzed successfully",
                engineResult.getReasons(),
                LocalDateTime.now()
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // GET transaction history for a user
    // ─────────────────────────────────────────────────────────────────
    public List<Transaction> getTransactionHistory(String senderId) {
        return transactionRepository.findBySenderId(senderId);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET all blocked transactions (admin dashboard)
    // ─────────────────────────────────────────────────────────────────
    public List<Transaction> getBlockedTransactions() {
        return transactionRepository.findByStatus(TransactionStatus.BLOCKED);
    }

    // ─────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────
    private void validateRequest(TransactionRequest request) {

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new InvalidTransactionException("amount",
                    "Amount must be greater than zero");
        }
        if (request.getSenderAccount() == null
                || request.getSenderAccount().isBlank()) {
            throw new InvalidTransactionException("senderId",
                    "Sender ID is required");
        }
        if (request.getReceiverUpiId() == null
                || request.getReceiverUpiId().isBlank()) {
            throw new InvalidTransactionException("receiverUpiId",
                    "Receiver UPI ID is required");
        }
        if (request.getSenderAccount().equals(request.getReceiverUpiId())) {
            throw new InvalidTransactionException(
                    "Self-transfer not allowed");
        }
    }

    private Transaction buildTransaction(TransactionRequest request) {
        Transaction tx = new Transaction();
        tx.setSenderId(request.getSenderAccount());
        tx.setReceiverUpiId(request.getReceiverUpiId());
        tx.setAmount(request.getAmount());
        tx.setDeviceId(request.getDeviceId());
        tx.setIpAddress(request.getIpAddress());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCreatedAt(LocalDateTime.now());
        return tx;
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score >= 71) return RiskLevel.BLOCK;
        if (score >= 31) return RiskLevel.SUSPICIOUS;
        return RiskLevel.SAFE;
    }

    private TransactionStatus determineStatus(int score) {
        if (score >= 71) return TransactionStatus.BLOCKED;
        if (score >= 31) return TransactionStatus.FLAGGED;
        return TransactionStatus.APPROVED;
    }
}