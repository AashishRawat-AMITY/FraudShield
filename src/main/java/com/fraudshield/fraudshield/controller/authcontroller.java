package com.fraudshield.fraudshield.controller;

import com.fraudshield.fraudshield.dto.AuthRequest;
import com.fraudshield.fraudshield.dto.AuthResponse;
import com.fraudshield.fraudshield.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class authcontroller {

    @Autowired
    private AuthService authService;

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/auth/register
    // PUBLIC — no JWT needed
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody AuthRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/auth/login
    // PUBLIC — no JWT needed
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}