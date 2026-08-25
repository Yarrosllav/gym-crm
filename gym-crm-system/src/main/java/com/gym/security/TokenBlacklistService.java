package com.gym.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String jti, Instant tokenExpiry) {
        blacklist.put(jti, tokenExpiry);
    }

    public boolean isBlacklisted(String jti) {
        return blacklist.containsKey(jti);
    }

    @Scheduled(fixedRate = 600_000)
    public void cleanExpired() {
        var now = Instant.now();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
