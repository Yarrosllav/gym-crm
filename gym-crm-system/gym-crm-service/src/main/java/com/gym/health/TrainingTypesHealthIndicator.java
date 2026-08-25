package com.gym.health;

import com.gym.service.impl.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("trainingTypes")
@RequiredArgsConstructor
public class TrainingTypesHealthIndicator implements HealthIndicator {

    private final TrainingTypeService trainingTypeService;

    @Override
    public Health health() {
        var count = trainingTypeService.getAll().size();
        if (count == 0) {
            return Health.down().withDetail("training types", "no training types seeded").build();
        }
        return Health.up().withDetail("training types", count).build();
    }
}
