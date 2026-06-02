package com.fraudshield.fraudshield.dto;



import com.fraudshield.fraudshield.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Sender account cannot be empty")
    private String senderAccount;   // Who is sending money

    @NotBlank(message = "Receiver UPI ID cannot be empty")
    private String receiverUpiId;   // Who is receiving money

    @NotNull(message = "Amount cannot be null")
    @Min(value = 1, message = "Amount must be at least 1 rupee")
    private Double amount;          // How much money

    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;   // UPI, NEFT, IMPS etc.

    @NotBlank(message = "Device ID cannot be empty")
    private String deviceId;        // Which device initiated this

    private String ipAddress;       // IP address of the request

    private String location;        // City/region
}