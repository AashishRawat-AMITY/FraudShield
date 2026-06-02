package com.fraudshield.fraudshield.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // ── Secret key — must be 256 bits minimum for HS256 ──────────────
    private static final String SECRET =
            "fraudshield-super-secret-key-2024-minimum-256-bits-long";

    private static final long EXPIRATION_MS = 86400000L; // 24 hours

    // ── Build the signing key ─────────────────────────────────────────
    // Return type is now SecretKey (not Key) — required by 0.12.x
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // ─────────────────────────────────────────────────────────────────
    // 1. GENERATE TOKEN
    // ─────────────────────────────────────────────────────────────────
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims(claims)                  // ← was setClaims()
                .subject(email)                  // ← was setSubject()
                .issuedAt(new Date())             // ← was setIssuedAt()
                .expiration(new Date(            // ← was setExpiration()
                        System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(),
                        Jwts.SIG.HS256)              // ← was SignatureAlgorithm.HS256
                .compact();
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. VALIDATE TOKEN
    // ─────────────────────────────────────────────────────────────────
    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. EXTRACT DATA FROM TOKEN
    // ─────────────────────────────────────────────────────────────────
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ─────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parser()                     // ← was parserBuilder()
                .verifyWith(getSigningKey())      // ← was setSigningKey()
                .build()
                .parseSignedClaims(token)         // ← was parseClaimsJws()
                .getPayload();                    // ← was getBody()
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public long getExpirationTime() {
        return EXPIRATION_MS;
    }
}