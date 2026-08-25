package com.gym.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter loginSuccess;
    private final Counter loginFailure;

    public GymMetrics(MeterRegistry registry) {
        this.traineeRegistrations = Counter.builder("gym.registrations")
                .tag("role", "trainee").description("Number of completed registrations").register(registry);
        this.trainerRegistrations = Counter.builder("gym.registrations")
                .tag("role", "trainer").description("Number of completed registrations").register(registry);
        this.loginSuccess = Counter.builder("gym.login.attempts")
                .tag("result", "success").description("Login attempts by outcome").register(registry);
        this.loginFailure = Counter.builder("gym.login.attempts")
                .tag("result", "failure").description("Login attempts by outcome").register(registry);
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrations.increment();
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrations.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccess.increment();
    }

    public void incrementLoginFailure() {
        loginFailure.increment();
    }
}
