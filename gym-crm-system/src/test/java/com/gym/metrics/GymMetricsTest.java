package com.gym.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GymMetricsTest {

    private SimpleMeterRegistry registry;
    private GymMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new GymMetrics(registry);
    }

    @Test
    void incrementTraineeRegistrations_shouldIncreaseCounter() {
        metrics.incrementTraineeRegistrations();
        metrics.incrementTraineeRegistrations();

        assertEquals(2.0, registry.get("gym.registrations").tag("role", "trainee").counter().count());
    }

    @Test
    void incrementLoginSuccessAndFailure_shouldTrackSeparately() {
        metrics.incrementLoginSuccess();
        metrics.incrementLoginFailure();
        metrics.incrementLoginFailure();

        assertEquals(1.0, registry.get("gym.login.attempts").tag("result", "success").counter().count());
        assertEquals(2.0, registry.get("gym.login.attempts").tag("result", "failure").counter().count());
    }
}
