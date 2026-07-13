package com.gym.service;

import com.gym.dao.ITraineeDao;
import com.gym.dao.ITrainerDao;
import com.gym.dao.ITrainingDao;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.service.impl.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private ITrainingDao trainingDao;
    @Mock
    private ITraineeDao traineeDao;
    @Mock
    private ITrainerDao trainerDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void addTraining_shouldCreateTraining_whenTraineeAndTrainerExist() {
        var trainee = new Trainee();
        trainee.setId(1L);
        var trainer = new Trainer();
        trainer.setId(2L);
        var specialization = new TrainingType();
        trainer.setSpecialization(specialization);

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(trainer));

        var result = trainingService.addTraining(1L, 2L, "Morning Run", LocalDate.of(2026, 6, 1), 60);

        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
        assertEquals(specialization, result.getTrainingType());
        verify(trainingDao).create(result);
    }

    @Test
    void addTraining_shouldThrow_whenTraineeNotFound() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(1L, 2L, "Run", LocalDate.now(), 60));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void addTraining_shouldThrow_whenTrainerNotFound() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(1L, 2L, "Run", LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenNameBlank() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(1L, 2L, " ", LocalDate.now(), 60));
        verifyNoInteractions(traineeDao, trainerDao, trainingDao);
    }

    @Test
    void addTraining_shouldThrow_whenDateOrDurationMissing() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(1L, 2L, "Run", null, 60));
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(1L, 2L, "Run", LocalDate.now(), null));
    }

    @Test
    void getTraineeTrainings_shouldDelegateToDao() {
        when(trainingDao.findByTraineeCriteria("John.Smith", null, null, null, null)).thenReturn(List.of());

        assertNotNull(trainingService.getTraineeTrainings("John.Smith", null, null, null, null));
        verify(trainingDao).findByTraineeCriteria("John.Smith", null, null, null, null);
    }

    @Test
    void getTrainerTrainings_shouldDelegateToDao() {
        when(trainingDao.findByTrainerCriteria("Jane.Doe", null, null, null)).thenReturn(List.of());

        assertNotNull(trainingService.getTrainerTrainings("Jane.Doe", null, null, null));
        verify(trainingDao).findByTrainerCriteria("Jane.Doe", null, null, null);
    }
}
