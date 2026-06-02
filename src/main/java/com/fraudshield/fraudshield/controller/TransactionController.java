package com.fraudshield.fraudshield.controller;

import com.fraudshield.fraudshield.dto.TransactionRequest;
import com.fraudshield.fraudshield.dto.TransactionResponse;
import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/transaction/analyze
    // PROTECTED — JWT required
    // THE most important endpoint in the entire project
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/analyze")
    public ResponseEntity<TransactionResponse> analyze(
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse response =
                transactionService.analyzeTransaction(request);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/transaction/history/{senderId}
    // PROTECTED — JWT required
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/history/{senderId}")
    public ResponseEntity<List<Transaction>> getHistory(
            @PathVariable String senderId) {

        List<Transaction> history =
                transactionService.getTransactionHistory(senderId);
        return ResponseEntity.ok(history);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/transaction/blocked
    // PROTECTED — JWT required (admin use)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/blocked")
    public ResponseEntity<List<Transaction>> getBlocked() {

        List<Transaction> blocked =
                transactionService.getBlockedTransactions();
        return ResponseEntity.ok(blocked);
    }
}