-- Create blacklisted_tokens table to store revoked JWT tokens
CREATE TABLE IF NOT EXISTS blacklisted_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    blacklisted_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at)
);

-- Note: Scheduled event for cleanup should be created manually in MySQL
-- or use the TokenCleanupService.java scheduled task instead
