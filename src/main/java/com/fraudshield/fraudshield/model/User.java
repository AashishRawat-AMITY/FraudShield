package com.fraudshield.fraudshield.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data                           // Lombok → generates getters, setters, toString
@Builder                        // Lombok → lets you build objects cleanly
@NoArgsConstructor              // Lombok → generates empty constructor
@AllArgsConstructor             // Lombok → generates constructor with all fields
@Document(collection = "users") // MongoDB → this class maps to "users" collection
public class User {

    @Id
    private String id;          // MongoDB generates this automatically (_id field)

    @Indexed(unique = true)     // No two users can have same email
    private String email;

    private String password;    // Will be stored as BCrypt hash, never plain text

    private String fullName;

    @Field("phone_number")      // In MongoDB it saves as "phone_number" not "phoneNumber"
    private String phoneNumber;

    private String role;        // "BANK", "ADMIN", "USER"

    private String bankName;

    @Field("is_active")
    private boolean isActive;   // false = account suspended

    @Field("created_at")
    private LocalDateTime createdAt;   // When this user registered

    @Field("last_login")
    private LocalDateTime lastLogin;   // Last time they logged in

    @Field("known_devices")
    private List<String> knownDevices; // List of device IDs this user normally uses
}
