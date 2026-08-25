package com.gym.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ServiceJwtServiceTest {

    private static final String SECRET = "ThisIsATestServiceSecretKeyLongEnoughForHmac256";

    @Test
    void generateServiceToken_shouldProduceNonEmptyToken() {
        var service = new ServiceJwtService(SECRET);

        assertNotNull(service.generateServiceToken());
    }

    @Test
    void generateServiceToken_shouldProduceDifferentTokensOnEachCall() {
        var service = new ServiceJwtService(SECRET);

        assertNotEquals(service.generateServiceToken(), service.generateServiceToken());
    }
}
