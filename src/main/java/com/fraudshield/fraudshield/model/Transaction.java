package com.fraudshield.fraudshield.model;

import com.fraudshield.fraudshield.enums.RiskLevel;
import com.fraudshield.fraudshield.enums.TransactionStatus;
import com.fraudshield.fraudshield.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @Field("sender_id")
    private String senderId;        // Links to User.id who sent the money

    @Field("sender_account")
    private String senderAccount;   // Sender's bank account number

    @Field("receiver_upi_id")
    private String receiverUpiId;   // Who the money is going to

    @Field("receiver_account")
    private String receiverAccount; // Receiver's account number

    private double amount;          // Transaction amount in rupees

    private TransactionType type;   // UPI, NEFT, IMPS, CARD_PAYMENT, WALLET

    private TransactionStatus status; // PENDING, APPROVED, FLAGGED, BLOCKED

    @Field("risk_score")
    private int riskScore;          // 0-100 score from the engine

    @Field("risk_level")
    private RiskLevel riskLevel;    // SAFE, SUSPICIOUS, BLOCK

    @Field("device_id")
    private String deviceId;        // Device this transaction came from

    @Field("ip_address")
    private String ipAddress;       // IP address of the request

    private String location;        // City/region of transaction

    @Field("transaction_time")
    private LocalDateTime transactionTime; // When transaction was initiated
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("analyzed_at")
    private LocalDateTime analyzedAt;      // When FraudShield analyzed it

    @Field("block_reason")
    private String blockReason;     // Why it was blocked (if blocked)
}
