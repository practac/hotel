package com.hotel.hotel.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    private final byte[] secret;
    private final long expirationMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
        this.secret = secret.getBytes();
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(String subject) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret))
                .compact();
    }

    public String validateAndGetSubject(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret))
                    .build()
                    .parseSignedClaims(token);
            return claims.getPayload().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalStateException("Invalid or expired token", e);
        }
    }
}
