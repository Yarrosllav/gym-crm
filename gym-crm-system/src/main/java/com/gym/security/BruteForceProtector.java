package com.gym.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BruteForceProtector {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 3 * 60 * 1000L;
    private final Map<String, Attempts> attemptsByUsername = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        var attempts = attemptsByUsername.get(username);
        if (attempts == null || attempts.lockedUntil() == null) {
            return false;
        }
        if (Instant.now().isAfter(attempts.lockedUntil())) {
            attemptsByUsername.remove(username);
            return false;
        }
        return true;
    }

    public void onFailure(String username) {
        var current = attemptsByUsername.getOrDefault(username, new Attempts(0, null));
        var newCount = current.count() + 1;
        var lockedUntil = newCount >= MAX_ATTEMPTS ? Instant.now().plusMillis(LOCK_DURATION_MS) : null;
        attemptsByUsername.put(username, new Attempts(newCount, lockedUntil));
    }

    public void onSuccess(String username) {
        attemptsByUsername.remove(username);
    }

    private record Attempts(int count, Instant lockedUntil) {
    }
}
