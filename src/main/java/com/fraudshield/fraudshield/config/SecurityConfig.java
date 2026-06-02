package com.fraudshield.fraudshield.config;



import com.fraudshield.fraudshield.security.JwtFilter;
import com.fraudshield.fraudshield.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {




    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // ─────────────────────────────────────────────────────────────────
    // BEAN 1: Password Encoder
    // Hashes passwords before saving. Verifies during login.
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────────────────────────────
    // BEAN 2: Authentication Provider
    // Wires together: where to find users + how to check passwords
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                customUserDetailsService
        );

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // ─────────────────────────────────────────────────────────────────
    // BEAN 3: Authentication Manager
    // The entry point Spring uses to trigger a login check.
    // AuthService calls this directly to verify credentials.
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─────────────────────────────────────────────────────────────────
    // BEAN 4: Security Filter Chain — THE MAIN RULE BOOK
    // Defines: which URLs are public, which need JWT,
    //          no sessions, and where JwtFilter sits in the pipeline
    // ─────────────────────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // ── Disable CSRF ─────────────────────────────────────────
                // CSRF protection is for browser-based apps with cookies.
                // Our API uses JWT tokens — CSRF is irrelevant and would
                // block all our POST requests without a browser form.
                .csrf(csrf -> csrf.disable())

                // ── URL Authorization Rules ───────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC — no token required
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // PUBLIC — health check (for load balancers, DevOps)
                        .requestMatchers("/actuator/health").permitAll()

                        // EVERYTHING ELSE — requires valid JWT token
                        .anyRequest().authenticated()
                )

                // ── No Sessions — Stateless API ───────────────────────────
                // Never create or use HTTP sessions.
                // Every request must carry its own JWT token.
                // This is what makes the API stateless and scalable.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Register Authentication Provider ─────────────────────
                .authenticationProvider(authenticationProvider())

                // ── Insert JwtFilter BEFORE Spring's default login filter ─
                // Spring has its own UsernamePasswordAuthenticationFilter.
                // Our JwtFilter must run BEFORE it so it can set the
                // authentication context that Spring's filter then reads.
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

