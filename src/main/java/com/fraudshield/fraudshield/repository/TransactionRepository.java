package com.fraudshield.fraudshield.repository;



import com.fraudshield.fraudshield.model.Transaction;
import com.fraudshield.fraudshield.enums.TransactionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    // Get all transactions by a specific user
    List<Transaction> findBySenderId(String senderId);

    // Get all transactions sent to a specific account (used in mule detection)
    List<Transaction> findByReceiverUpiId(String receiverUpiId);

    // Get transactions of a user within a time window (behavioral analysis)
    List<Transaction> findBySenderIdAndCreatedAtBetween(
            String senderId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Count how many people sent money to this receiver (mule pattern check)
    long countByReceiverUpiIdAndCreatedAtAfter(
            String receiverUpiId,
            LocalDateTime after
    );

    // Get all blocked transactions (for fraud dashboard)
    List<Transaction> findByStatus(TransactionStatus status);
}
