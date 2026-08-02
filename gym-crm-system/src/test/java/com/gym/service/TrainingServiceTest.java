package com.gym.service;

import com.gym.dao.ITraineeDao;
import com.gym.dao.ITrainerDao;
import com.gym.dao.ITrainingDao;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
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

    private Trainee buildTrainee(String username, String password, boolean active) {
        var user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(active);

        var trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);
        return trainee;
    }

    private Trainer buildTrainer(String username, String password, boolean active) {
        var user = new User();
        user.setFirstName("Mike");
        user.setLastName("Jones");
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(active);

        var trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUser(user);
        return trainer;
    }

    @Test
    void addTraining_shouldCreateTraining_whenTraineeAndTrainerExist() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        var trainer = buildTrainer("Mike.Jones", "pwd", true);
        var specialization = new TrainingType();
        trainer.setSpecialization(specialization);

        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Jones")).thenReturn(Optional.of(trainer));

        var result = trainingService.addTraining("John.Smith", "Mike.Jones", "Morning Run", LocalDate.of(2026, 6, 1), 60);

        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
        assertEquals(specialization, result.getTrainingType());
        verify(trainingDao).create(result);
    }

    @Test
    void addTraining_shouldThrow_whenTraineeNotFound() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining("John.Smith", "Mike.Jones", "Run", LocalDate.now(), 60));
        verifyNoInteractions(trainingDao);
    }

    @Test
    void addTraining_shouldThrow_whenTrainerNotFound() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findByUsername("Mike.Jones")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining("John.Smith", "Mike.Jones", "Run", LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenNameBlank() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining("John.Smith", "Mike.Jones", " ", LocalDate.now(), 60));
        verifyNoInteractions(traineeDao, trainerDao, trainingDao);
    }

    @Test
    void addTraining_shouldThrow_whenDateOrDurationMissing() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining("John.Smith", "Mike.Jones", "Run", null, 60));
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining("John.Smith", "Mike.Jones", "Run", LocalDate.now(), null));
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
