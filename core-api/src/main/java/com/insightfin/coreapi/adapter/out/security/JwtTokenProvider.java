package com.insightfin.coreapi.adapter.out.security;

import com.insightfin.coreapi.domain.model.Role;
import com.insightfin.coreapi.domain.port.out.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@ApplicationScoped
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    @Inject
    public JwtTokenProvider(@ConfigProperty(name = "jwt.secret") String secret,
                            @ConfigProperty(name = "jwt.expiration-ms") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(UUID userId, String email, Role role, boolean emailVerified) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .claim("email_verified", emailVerified)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public Role extractRole(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role", String.class);
        return role != null ? Role.valueOf(role) : Role.USER;
    }

    @Override
    public boolean isEmailVerified(String token) {
        Claims claims = parseClaims(token);
        Boolean verified = claims.get("email_verified", Boolean.class);
        return verified != null && verified;
    }

    @Override
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
