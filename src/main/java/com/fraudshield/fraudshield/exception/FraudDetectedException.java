package com.fraudshield.fraudshield.exception;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class FraudDetectedException extends RuntimeException {

    private final int riskScore;
    private final String riskLevel;

    public FraudDetectedException(int riskScore, String riskLevel) {
        super("Transaction blocked due to fraud detection. Risk Score: "
                + riskScore + " | Risk Level: " + riskLevel);
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}
