package com.fraudshield.fraudshield.service;

import com.fraudshield.fraudshield.dto.AuthRequest;
import com.fraudshield.fraudshield.dto.AuthResponse;
import com.fraudshield.fraudshield.service.AuditService;
import com.fraudshield.fraudshield.model.User;
import com.fraudshield.fraudshield.repository.UserRepository;
import com.fraudshield.fraudshield.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuditService auditService;

    // ─────────────────────────────────────────────────────────────────
    // REGISTER — create a new bank/client account
    // ─────────────────────────────────────────────────────────────────
    public AuthResponse register(AuthRequest request) {

        // Step 1: Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: "
                    + request.getEmail());
        }

        // Step 2: Build the User object
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword()) // BCrypt hash
        );
        user.setCreatedAt(LocalDateTime.now());

        // Step 3: Save to MongoDB
        User savedUser = userRepository.save(user);

        // Step 4: Generate JWT token immediately
        // New users are logged in right after registration
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Step 5: Write audit log
        auditService.log(
                "USER_REGISTERED",
                "New account registered: " + savedUser.getEmail(),
                savedUser.getId()
        );

        // Step 6: Return token + user info
//        return new AuthResponse(token, savedUser.getEmail(), "Registration successful");
        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getRole(),
                jwtUtil.getExpirationTime(),"REGISTRATION SUCCESSFULL"
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // LOGIN — verify credentials and return JWT token
    // ─────────────────────────────────────────────────────────────────
    public AuthResponse login(AuthRequest request) {

        // Step 1: Trigger Spring Security's full auth pipeline
        // This calls: CustomUserDetailsService → UserRepository → BCrypt.matches()
        // If credentials are wrong → BadCredentialsException → GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Step 2: If we reach here — credentials were valid
        // Load the user to get their details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found after authentication")
                );

        // Step 3: Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // Step 4: Write audit log
        auditService.log(
                "USER_LOGIN",
                "Successful login: " + user.getEmail(),
                user.getId()
        );

        // Step 5: Return token
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole(),
                jwtUtil.getExpirationTime(),"LOGIN SUCCESSFUL"
        );
    }


}