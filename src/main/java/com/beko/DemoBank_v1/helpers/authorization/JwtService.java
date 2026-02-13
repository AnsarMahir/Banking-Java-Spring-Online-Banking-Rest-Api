package com.beko.DemoBank_v1.helpers.authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {
    private final String appSecret;
    private final long expiresInSeconds;
    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    public JwtService(@Value("${demoBank.app.secret}") String appSecret,
                      @Value("${demoBank.app.expires.in}") long expiresInSeconds) {
        this.appSecret = appSecret;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String generateToken(String userEmail) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + (expiresInSeconds * 1000L));
        Key secretKey = Keys.hmacShaKeyFor(appSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userEmail)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims decodeToken(String token) {
        try {
            Key secretKey = Keys.hmacShaKeyFor(appSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenIncluded(String req) {
        return req != null && req.startsWith("Bearer ");
    }

    public String getAccessTokenFromHeader(String req) {
        String[] parts = req.split(" ");
        if (parts.length != 2 || parts[1] == null || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }
        return parts[1];
    }

    public void revokeToken(String token) {
        Claims claims = decodeToken(token);
        long expiry = claims != null && claims.getExpiration() != null
                ? claims.getExpiration().getTime()
                : System.currentTimeMillis() + (expiresInSeconds * 1000L);
        revokedTokens.put(token, expiry);
    }

    public boolean isTokenRevoked(String token) {
        cleanupExpiredRevocations();
        Long expiry = revokedTokens.get(token);
        if (expiry == null) {
            return false;
        }
        if (expiry <= System.currentTimeMillis()) {
            revokedTokens.remove(token);
            return false;
        }
        return true;
    }

    private void cleanupExpiredRevocations() {
        long now = System.currentTimeMillis();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() <= now);
    }
}
