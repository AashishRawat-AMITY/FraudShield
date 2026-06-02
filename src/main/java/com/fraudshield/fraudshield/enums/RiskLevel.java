package com.fraudshield.fraudshield.enums;

public enum RiskLevel {

    SAFE,        // Score 0-30  → Transaction is normal, allow it
    SUSPICIOUS,  // Score 31-70 → Something looks off, trigger OTP
    BLOCK        // Score 71-100 → Fraud detected, stop the transaction
}