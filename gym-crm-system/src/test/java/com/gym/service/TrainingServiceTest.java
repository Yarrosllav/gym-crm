package com.gym.service;

import com.gym.dao.TrainingDAO;
import com.gym.model.Training;
import com.gym.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    @Mock
    private TrainingDAO trainingDAO;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setTrainingName("Test Training");
        training.setTrainingDuration(60.0);
    }

    @Test
    void testCreateTraining() {
        trainingService.create(training);

        verify(trainingDAO, times(1)).create(training);
    }

    @Test
    void testGetTraining() {
        var id = UUID.randomUUID();
        when(trainingDAO.findById(id)).thenReturn(training);

        var result = trainingService.findById(id);

        assertEquals(training, result);
        verify(trainingDAO, times(1)).findById(id);
    }
}
