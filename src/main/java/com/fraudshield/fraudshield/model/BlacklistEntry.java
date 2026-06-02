package com.fraudshield.fraudshield.model;



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
@Document(collection = "blacklist")
public class BlacklistEntry {

    @Id
    private String id;

    @Field("upi_id")
    private String upiId;           // Blacklisted UPI ID

    @Field("account_number")
    private String accountNumber;   // Blacklisted account number

    @Field("device_id")
    private String deviceId;        // Blacklisted device

    private String reason;          // Why this entry was blacklisted

    @Field("reported_count")
    private int reportedCount;      // How many times reported across banks

    @Field("added_at")
    private LocalDateTime addedAt;  // When it was blacklisted

    @Field("added_by")
    private String addedBy;         // Which bank/system added it


    @Field("device_fingerprint")
    private String deviceFingerprint;

    @Field("ip_address")
    private String ipAddress;

    @Field("is_active")
    private Boolean active = true;
}
