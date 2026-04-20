package com.enterprise.wms.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${wms.security.jwt-secret}")
    private String secret;

    @Value("${wms.security.jwt-expiry-ms}")
    private long expiryMs;

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        Claims claims = parseClaims(token);
        return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey secretKey() {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(key.length >= 32 ? key : String.format("%-32s", secret).getBytes(StandardCharsets.UTF_8));
    }
}
