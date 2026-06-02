package com.fraudshield.fraudshield.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;       // The JWT token bank app uses for future requests

    private String email;       // Confirms which account logged in

    private String role;        // "BANK", "ADMIN", "USER"

    private long expiresIn;     // How many milliseconds until token expires
    private String message; // RESPONSE
}
