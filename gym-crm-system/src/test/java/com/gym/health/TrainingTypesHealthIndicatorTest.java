package com.gym.health;

import com.gym.model.TrainingType;
import com.gym.service.impl.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingTypesHealthIndicatorTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @Test
    void health_shouldReturnUp_whenTrainingTypesExist() {
        when(trainingTypeService.getAll()).thenReturn(List.of(new TrainingType()));

        assertEquals(Status.UP, new TrainingTypesHealthIndicator(trainingTypeService).health().getStatus());
    }

    @Test
    void health_shouldReturnDown_whenNoTrainingTypesSeeded() {
        when(trainingTypeService.getAll()).thenReturn(List.of());

        assertEquals(Status.DOWN, new TrainingTypesHealthIndicator(trainingTypeService).health().getStatus());
    }
}
