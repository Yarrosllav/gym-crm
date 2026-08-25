package com.gym.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceTest {

    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        blacklistService = new TokenBlacklistService();
    }

    @Test
    void isBlacklisted_shouldReturnFalse_forUnknownJti() {
        assertFalse(blacklistService.isBlacklisted("unknown-jti"));
    }

    @Test
    void isBlacklisted_shouldReturnTrue_afterBlacklisting() {
        blacklistService.blacklist("some-jti", Instant.now().plusSeconds(3600));

        assertTrue(blacklistService.isBlacklisted("some-jti"));
    }

    @Test
    void cleanExpired_shouldRemoveOnlyExpiredEntries() {
        blacklistService.blacklist("expired-jti", Instant.now().minusSeconds(10));
        blacklistService.blacklist("active-jti", Instant.now().plusSeconds(3600));

        blacklistService.cleanExpired();

        assertFalse(blacklistService.isBlacklisted("expired-jti"));
        assertTrue(blacklistService.isBlacklisted("active-jti"));
    }
}
