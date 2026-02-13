package com.beko.DemoBank_v1.repository;

import com.beko.DemoBank_v1.models.BlacklistedToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class BlacklistedTokenRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Add token to blacklist
    public void blacklistToken(String token, LocalDateTime expiresAt) {
        String sql = "INSERT INTO blacklisted_tokens (token, blacklisted_at, expires_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, token, LocalDateTime.now(), expiresAt);
    }

    // Check if token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        String sql = "SELECT COUNT(*) FROM blacklisted_tokens WHERE token = ? AND expires_at > ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, token, LocalDateTime.now());
        return count != null && count > 0;
    }

    // Clean up expired tokens (should be run periodically)
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM blacklisted_tokens WHERE expires_at <= ?";
        jdbcTemplate.update(sql, LocalDateTime.now());
    }
}
