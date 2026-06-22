package com.nexushr.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(@Value("${app.security.jwt-secret}") String secret,
                      @Value("${app.security.jwt-ttl-seconds:3600}") long ttlSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    // 🌟 Enhanced method to handle clean String serialization for roles
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();

        // Convert GrantedAuthorities (e.g. "ROLE_EMPLOYEE") to pure strings (e.g. "EMPLOYEE")
        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(Map.of("roles", roles)) // 🌟 Placed explicitly as plain text strings
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    // Keep your original method intact if other components call it directly
    public String issue(AppUser user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of("uid", user.getId().toString(), "roles", roles))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}