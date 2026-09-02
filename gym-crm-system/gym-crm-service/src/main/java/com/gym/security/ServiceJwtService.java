package com.gym.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class ServiceJwtService {

    private final SecretKey key;

    public ServiceJwtService(@Value("${service.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateServiceToken() {
        var now = new Date();
        var expiry = new Date(now.getTime() + 60_000);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("gym-crm-service")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
