package com.beko.DemoBank_v1.helpers.authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    // CRITICAL FIX (V-01): Use @Value to inject secret from properties instead of hardcoding
    @Value("${demoBank.app.secret}")
    private String appSecret;

    @Value("${demoBank.app.expires.in}")
    private long expiresIn;

    public String generateToken(String userEmail) {
        Date now = new Date();

        // CRITICAL FIX (V-08): Multiply by 1000 to convert seconds to milliseconds
        Date expirationDate = new Date(now.getTime() + (expiresIn * 1000));

        // CRITICAL FIX (V-35): Use proper key derivation instead of raw string
        Key secretKey = Keys.hmacShaKeyFor(appSecret.getBytes());

        String token = Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        logger.debug("Generated JWT token for user: {}", userEmail);
        return token;
    }

    public Claims decodeToken(String token) {
        try {
            // CRITICAL FIX (V-35): Use proper key derivation
            Key secretKey = Keys.hmacShaKeyFor(appSecret.getBytes());

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return claimsJws.getBody();
        } catch (Exception e) {
            // CRITICAL FIX (V-06): Return null on invalid token (caller must check for null)
            logger.warn("Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenIncluded(String req) {
        return req != null && !req.isEmpty();
    }

    public String getAccessTokenFromHeader(String req) {
        // CRITICAL FIX (V-36): Add validation before splitting
        if (req == null || !req.contains(" ")) {
            logger.warn("Invalid authorization header format");
            return null;
        }

        String[] parts = req.split(" ");

        if (parts.length != 2) {
            logger.warn("Authorization header does not contain exactly 2 parts");
            return null;
        }

        if (!"Bearer".equalsIgnoreCase(parts[0])) {
            logger.warn("Authorization header does not start with 'Bearer'");
            return null;
        }

        return parts[1];
    }

    public Date getTokenExpiration(String token) {
        try {
            Claims claims = decodeToken(token);
            return claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            System.out.println("Could not get token expiration.");
            return null;
        }
    }
}