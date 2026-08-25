package com.gym.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BruteForceProtectorTest {

    private BruteForceProtector protector;

    @BeforeEach
    void setUp() {
        protector = new BruteForceProtector();
    }

    @Test
    void isBlocked_shouldReturnFalse_forUnknownUsername() {
        assertFalse(protector.isBlocked("John.Smith"));
    }

    @Test
    void isBlocked_shouldReturnFalse_afterTwoFailures() {
        protector.onFailure("John.Smith");
        protector.onFailure("John.Smith");

        assertFalse(protector.isBlocked("John.Smith"));
    }

    @Test
    void isBlocked_shouldReturnTrue_afterThreeFailures() {
        protector.onFailure("John.Smith");
        protector.onFailure("John.Smith");
        protector.onFailure("John.Smith");

        assertTrue(protector.isBlocked("John.Smith"));
    }

    @Test
    void onSuccess_shouldResetFailureCount() {
        protector.onFailure("John.Smith");
        protector.onFailure("John.Smith");
        protector.onSuccess("John.Smith");
        protector.onFailure("John.Smith");
        protector.onFailure("John.Smith");

        assertFalse(protector.isBlocked("John.Smith"));
    }

    @Test
    void isBlocked_shouldNotAffectOtherUsernames() {
        protector.onFailure("John.Smith");
        protector.onFailure("John.Smith");
        protector.onFailure("John.Smith");

        assertFalse(protector.isBlocked("Jane.Doe"));
    }
}
