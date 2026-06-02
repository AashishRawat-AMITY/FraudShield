package com.fraudshield.fraudshield.exception;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransactionException extends RuntimeException {

    private final String field;
    private final String reason;

    public InvalidTransactionException(String field, String reason) {
        super("Invalid transaction - Field: [" + field + "] Reason: " + reason);
        this.field = field;
        this.reason = reason;
    }

    // Single message constructor (for simple cases)
    public InvalidTransactionException(String message) {
        super(message);
        this.field = "unknown";
        this.reason = message;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}