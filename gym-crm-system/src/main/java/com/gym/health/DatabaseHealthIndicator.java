package com.gym.health;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("database")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final SessionFactory sessionFactory;

    @Override
    public Health health() {
        try (var session = sessionFactory.openSession()) {
            session.createNativeQuery("SELECT 1", Integer.class).getSingleResult();
            return Health.up().withDetail("database", "reachable").build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("database", "unreachable").build();
        }
    }
}
