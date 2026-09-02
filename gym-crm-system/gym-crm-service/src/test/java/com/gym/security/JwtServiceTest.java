package com.gym.security;

import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "TestSecretKeyThatIsLongEnoughForHmacSha256";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000L);
    }

    @Test
    void generateToken_shouldContainUsernameAndBeValid() {
        var token = jwtService.generateToken("John.Smith", "ROLE_TRAINEE");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("John.Smith", jwtService.extractUsername(token));
    }

    @Test
    void generateToken_shouldProduceUniqueJtiEachTime() {
        var token1 = jwtService.generateToken("John.Smith", "ROLE_TRAINEE");
        var token2 = jwtService.generateToken("John.Smith", "ROLE_TRAINEE");

        assertNotEquals(jwtService.extractJti(token1), jwtService.extractJti(token2));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenExpired() {
        var shortLivedService = new JwtService(SECRET, -1000L);
        var token = shortLivedService.generateToken("John.Smith", "ROLE_TRAINEE");

        assertFalse(shortLivedService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenMalformed() {
        assertFalse(jwtService.isTokenValid("not.a.valid.token"));
    }

    @Test
    void parseClaims_shouldThrow_whenSignedWithDifferentSecret() {
        var otherService = new JwtService("AnotherCompletelyDifferentSecretKeyForTesting", 3600000L);
        var token = otherService.generateToken("John.Smith", "ROLE_TRAINEE");

        assertThrows(SignatureException.class, () -> jwtService.parseClaims(token));
    }

    @Test
    void extractExpiration_shouldBeAfterIssuedTime() {
        var token = jwtService.generateToken("John.Smith", "ROLE_TRAINEE");

        assertTrue(jwtService.extractExpiration(token).after(new Date(System.currentTimeMillis() - 1000)));
    }
}
