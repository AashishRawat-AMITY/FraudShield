package com.fraudshield.fraudshield.repository;



import com.fraudshield.fraudshield.model.BlacklistEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface BlacklistRepository
        extends MongoRepository<BlacklistEntry, String> {

    // Check if a UPI ID is blacklisted
    Optional<BlacklistEntry> findByUpiId(String upiId);

    // Check if a device fingerprint is blacklisted
    Optional<BlacklistEntry> findByDeviceFingerprint(String deviceFingerprint);

    // Check if an IP address is blacklisted
    Optional<BlacklistEntry> findByIpAddress(String ipAddress);

    // Get all active blacklist entries
    List<BlacklistEntry> findByActiveTrue();

    // Check if UPI ID is blacklisted and active
    boolean existsByUpiIdAndActiveTrue(String upiId);
}