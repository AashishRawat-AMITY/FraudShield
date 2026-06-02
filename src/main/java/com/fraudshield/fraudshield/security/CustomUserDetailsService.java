package com.fraudshield.fraudshield.security;



import com.fraudshield.fraudshield.model.User;
import com.fraudshield.fraudshield.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────
    // Spring Security calls this automatically — you never call it directly
    // ─────────────────────────────────────────────────────────────────
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Step 1: Find user in MongoDB by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email)
                );

        // Step 2: Wrap our User model into Spring's UserDetails format
        // ArrayList() = no authorities/roles for now (we add roles later)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}