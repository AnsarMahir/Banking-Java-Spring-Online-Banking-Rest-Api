package com.beko.DemoBank_v1.models;

import java.time.LocalDateTime;

public class BlacklistedToken {
    private int id;
    private String token;
    private LocalDateTime blacklistedAt;
    private LocalDateTime expiresAt;

    public BlacklistedToken() {
    }

    public BlacklistedToken(String token, LocalDateTime blacklistedAt, LocalDateTime expiresAt) {
        this.token = token;
        this.blacklistedAt = blacklistedAt;
        this.expiresAt = expiresAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(LocalDateTime blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
