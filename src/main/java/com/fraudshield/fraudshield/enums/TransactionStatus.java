package com.fraudshield.fraudshield.enums;

public enum TransactionStatus {

    PENDING,    // Transaction just received, analysis not done yet
    APPROVED,   // Risk score was SAFE, transaction allowed through
    FLAGGED,    // Risk score was SUSPICIOUS, sent for extra verification
    BLOCKED     // Risk score was BLOCK, transaction stopped completely
}
