package com.beko.DemoBank_v1.service;

import com.beko.DemoBank_v1.repository.BlacklistedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TokenCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    // Run every day at 2 AM to clean up expired tokens
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        try {
            logger.info("Starting cleanup of expired blacklisted tokens");
            blacklistedTokenRepository.deleteExpiredTokens();
            logger.info("Cleanup of expired blacklisted tokens completed successfully");
        } catch (Exception e) {
            logger.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
