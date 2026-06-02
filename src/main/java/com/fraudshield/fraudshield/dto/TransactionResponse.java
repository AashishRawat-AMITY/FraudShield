package com.fraudshield.fraudshield.dto;



import com.fraudshield.fraudshield.enums.RiskLevel;
import com.fraudshield.fraudshield.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;       // ID of the analyzed transaction

    private TransactionStatus status;   // APPROVED, FLAGGED, or BLOCKED

    private RiskLevel riskLevel;        // SAFE, SUSPICIOUS, or BLOCK

    private int riskScore;              // 0-100 score

    private String message;            // Human readable result
    // "Transaction approved"
    // "Transaction blocked - high fraud risk"

    private String blockReason;         // Only filled if BLOCKED

    private LocalDateTime analyzedAt;   // When the analysis happened
}
