package com.gym.service;

import com.gym.dao.IReadOnlyDao;
import com.gym.dao.ITrainerDao;
import com.gym.exception.AuthenticationException;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.service.impl.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private ITrainerDao trainerDao;
    @Mock
    private IReadOnlyDao<TrainingType, Long> trainingTypeDao;
    @Mock
    private UsernameGenerator usernameGenerator;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(trainerDao, trainingTypeDao, usernameGenerator);
    }

    private Trainer buildTrainer(String username, String password, boolean active) {
        var user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(active);

        var trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        return trainer;
    }

    @Test
    void createProfile_shouldPersistTrainerWithResolvedSpecialization() {
        var specialization = new TrainingType(5L, "YOGA");

        when(trainingTypeDao.findById(5L)).thenReturn(Optional.of(specialization));
        when(usernameGenerator.generate("Jane", "Doe")).thenReturn("Jane.Doe");

        var result = trainerService.createProfile("Jane", "Doe", 5L);

        assertEquals("Jane.Doe", result.getUser().getUsername());
        assertEquals(specialization, result.getSpecialization());

        assertNotNull(result.getUser().getPassword());
        assertEquals(10, result.getUser().getPassword().length());

        verify(trainerDao).create(result);
    }

    @Test
    void createProfile_shouldThrow_whenSpecializationNotFound() {
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.createProfile("Jane", "Doe", 99L));
        verifyNoInteractions(trainerDao);
    }

    @Test
    void createProfile_shouldThrow_whenNameMissing() {
        assertThrows(ValidationException.class, () -> trainerService.createProfile("", "Doe", 5L));
        verifyNoInteractions(trainingTypeDao);
    }

    @Test
    void updateProfileAndStatus_shouldUpdateNameAndKeepSpecializationReadOnly() {
        var originalSpecialization = new TrainingType(5L, "YOGA");
        var trainer = buildTrainer("Jane.Doe", "pwd", true);
        trainer.setSpecialization(originalSpecialization);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        var result = trainerService.updateProfileAndStatus("Jane.Doe", "pwd",
                "Janet", "Doey", false);

        assertEquals("Janet", result.getUser().getFirstName());
        assertEquals("Doey", result.getUser().getLastName());
        assertEquals(originalSpecialization, result.getSpecialization());
        assertFalse(result.getUser().isActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void updateProfileAndStatus_shouldThrow_whenNameMissing() {
        var trainer = buildTrainer("Jane.Doe", "pwd", true);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class,
                () -> trainerService.updateProfileAndStatus("Jane.Doe", "pwd", " ",
                        "Doey", true));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void updateProfileAndStatus_shouldThrow_whenPasswordWrong() {
        var trainer = buildTrainer("Jane.Doe", "pwd", true);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        assertThrows(AuthenticationException.class,
                () -> trainerService.updateProfileAndStatus("Jane.Doe", "wrong", "Janet",
                        "Doey", true));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void matchCredentials_shouldReturnTrue_whenPasswordMatches() {
        var trainer = buildTrainer("Jane.Doe", "secret", true);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        assertTrue(trainerService.matchCredentials("Jane.Doe", "secret"));
    }

    @Test
    void setActive_shouldDeactivate_whenCurrentlyActive() {
        var trainer = buildTrainer("Jane.Doe", "pwd", true);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        trainerService.setActive("Jane.Doe", "pwd", false);

        assertFalse(trainer.getUser().isActive());
    }

    @Test
    void setActive_shouldThrow_whenCurrentlyActive() {
        var trainer = buildTrainer("John.Smith", "pwd", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class, () -> trainerService.setActive("John.Smith", "pwd", true));
        verify(trainerDao, never()).update(trainer);
    }

    @Test
    void getTrainersNotAssigned_shouldDelegateToDao() {
        when(trainerDao.findNotAssignedToTrainee("John.Smith")).thenReturn(List.of(new Trainer()));

        assertEquals(1, trainerService.getTrainersNotAssignedToTrainee("John.Smith").size());
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        var trainer = buildTrainer("Jane.Doe", "old", true);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        trainerService.changePassword("Jane.Doe", "old", "newPassword1");

        assertEquals("newPassword1", trainer.getUser().getPassword());
    }
}
