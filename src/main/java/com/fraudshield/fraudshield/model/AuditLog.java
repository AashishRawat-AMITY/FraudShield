package com.fraudshield.fraudshield.model;



import com.fraudshield.fraudshield.enums.RiskLevel;
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
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Field("transaction_id")
    private String transactionId;   // Which transaction this log is for

    @Field("user_id")
    private String userId;          // Which user was involved

    private String action;          // "TRANSACTION_ANALYZED", "TRANSACTION_BLOCKED" etc.

    @Field("risk_score")
    private int riskScore;          // Score at time of decision

    @Field("risk_level")
    private RiskLevel riskLevel;    // Decision made

    private String reason;          // Human readable reason for the decision

    @Field("performed_at")
    private LocalDateTime performedAt; // Exact timestamp — immutable record

    private String description;

    private String targetId;

    private String performedBy;

    private LocalDateTime createdAt;
}
