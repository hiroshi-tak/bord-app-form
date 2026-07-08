package com.example.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.backend.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static final long EXPIRATION = 1000 * 60 * 15;

    // ===== generate =====

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(key)
                .compact();
    }

    // ===== extract =====

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    // ===== validate =====

    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ===== internal =====

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}