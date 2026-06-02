package com.fraudshield.fraudshield.controller;

import com.fraudshield.fraudshield.model.User;
import com.fraudshield.fraudshield.repository.UserRepository;
import com.fraudshield.fraudshield.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/account/profile
    // Returns the logged-in bank's profile
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {

        // Get email from JWT token (already validated by JwtFilter)
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Clear password before returning — never expose it
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }

    // ─────────────────────────────────────────────────────────────────
    // PUT /api/v1/account/profile
    // Update account details
    // ─────────────────────────────────────────────────────────────────
    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestBody Map<String, String> updates) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Only allow safe fields to be updated
        if (updates.containsKey("bankName")) {
            user.setBankName(updates.get("bankName"));
        }

        userRepository.save(user);

        auditService.log(
                "PROFILE_UPDATED",
                "Profile updated for: " + email
        );

        return ResponseEntity.ok(
                Map.of("message", "Profile updated successfully")
        );
    }
}