package com.fraudshield.fraudshield.enums;

public enum TransactionType {
    UPI,            // UPI payment (GPay, PhonePe, Paytm etc.)
    NEFT,           // Bank to bank transfer (takes hours)
    IMPS,           // Instant bank transfer
    CARD_PAYMENT,   // Debit/Credit card transaction
    WALLET          // Digital wallet payment
}
