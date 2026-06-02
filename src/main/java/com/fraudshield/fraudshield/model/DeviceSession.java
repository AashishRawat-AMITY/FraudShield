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
@Document(collection = "device_sessions")
public class DeviceSession {

    @Id
    private String id;

    @Field("user_id")
    private String userId;          // Which user owns this device

    @Field("device_id")
    private String deviceId;        // Unique device fingerprint

    @Field("device_name")
    private String deviceName;      // "Samsung Galaxy S23", "iPhone 15" etc.

    @Field("ip_address")
    private String ipAddress;       // Last known IP of this device

    private String location;        // Last known location

    @Field("is_trusted")
    private boolean isTrusted;      // true = user has used this device before

    @Field("first_seen")
    private LocalDateTime firstSeen;   // When this device first appeared

    @Field("last_seen")
    private LocalDateTime lastSeen;    // Most recent transaction from this device
}
