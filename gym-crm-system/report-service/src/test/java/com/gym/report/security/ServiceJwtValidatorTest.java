package com.gym.report.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceJwtValidatorTest {

    private static final String SECRET = "ThisIsATestServiceSecretKeyLongEnoughForHmac256";

    @Test
    void isValid_shouldReturnTrue_forValidToken() {
        var validator = new ServiceJwtValidator(SECRET);
        var token = io.jsonwebtoken.Jwts.builder()
                .subject("gym-crm-service")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        assertTrue(validator.isValid(token));
    }

    @Test
    void isValid_shouldReturnFalse_forExpiredToken() {
        var validator = new ServiceJwtValidator(SECRET);
        var token = io.jsonwebtoken.Jwts.builder()
                .subject("gym-crm-service")
                .issuedAt(new java.util.Date(System.currentTimeMillis() - 120000))
                .expiration(new java.util.Date(System.currentTimeMillis() - 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        assertFalse(validator.isValid(token));
    }

    @Test
    void isValid_shouldReturnFalse_forGarbageToken() {
        var validator = new ServiceJwtValidator(SECRET);

        assertFalse(validator.isValid("not-a-jwt"));
    }

    @Test
    void isValid_shouldReturnFalse_whenSignedWithDifferentSecret() {
        var validator = new ServiceJwtValidator(SECRET);
        var token = io.jsonwebtoken.Jwts.builder()
                .subject("gym-crm-service")
                .expiration(new java.util.Date(System.currentTimeMillis() + 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("CompletelyDifferentSecretKeyForTesting".getBytes()))
                .compact();

        assertFalse(validator.isValid(token));
    }
}
