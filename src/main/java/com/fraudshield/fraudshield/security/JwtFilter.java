package com.fraudshield.fraudshield.security;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private com.fraudshield.fraudshield.security.JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // ─────────────────────────────────────────────────────────────────
    // This method runs ONCE for every HTTP request to your API
    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // ── Step 1: Get the Authorization header ─────────────────────
        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        // ── Step 2: Check format — must start with "Bearer " ─────────
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);     // remove "Bearer " prefix
            email = jwtUtil.extractEmail(jwt); // read who sent this
        }

        // ── Step 3: Validate — only if email found & not already auth'd
        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user from MongoDB
            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(email);

            // Validate token against user
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // ── Step 4: Set authentication in Spring Security ─────
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // no credentials needed
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // This line tells Spring Security: request is authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ── Step 5: Always continue the filter chain ──────────────────
        // Whether token valid or not — let Spring Security handle the rest
        filterChain.doFilter(request, response);
    }
}