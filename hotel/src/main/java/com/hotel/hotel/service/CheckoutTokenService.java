package com.hotel.hotel.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Issues and validates short-lived checkout tokens tied to a room number.
 * Token format: base64url(payload).base64url(hmacSha256(payload))
 * payload: roomNumber|expEpochSeconds|nonce
 */
@Component
public class CheckoutTokenService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private final byte[] secret;
    private final long ttlSeconds;

    public CheckoutTokenService(
            @Value("${checkout.token.secret:change-me}") String secret,
            @Value("${checkout.token.ttl-seconds:600}") long ttlSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
    }

    /** Issue a token bound to the given room number. */
    public String issue(String roomNumber) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = roomNumber + "|" + exp + "|" + UUID.randomUUID();
        String sig = signBase64(payload);
        return base64Url(payload) + "." + sig;
    }

    /** Validate token and return the room number if valid. */
    public String validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw bad("Invalid token format");
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String sig = parts[1];

        if (!signBase64(payload).equals(sig)) {
            throw bad("Invalid token signature");
        }

        String[] fields = payload.split("\\|");
        if (fields.length < 2) {
            throw bad("Invalid token payload");
        }
        String roomNumber = fields[0];
        long exp;
        try {
            exp = Long.parseLong(fields[1]);
        } catch (NumberFormatException e) {
            throw bad("Invalid token expiry");
        }
        long now = Instant.now().getEpochSecond();
        if (now > exp) {
            throw bad("Token expired");
        }
        return roomNumber;
    }

    private String signBase64(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret, HMAC_ALGO));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Cannot create token signature", e);
        }
    }

    private String base64Url(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
