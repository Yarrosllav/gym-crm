package com.gym.service;

import com.gym.dao.IReadOnlyDao;
import com.gym.dao.ITrainerDao;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.metrics.GymMetrics;
import com.gym.model.Role;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.service.impl.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    @Mock
    private GymMetrics gymMetrics;
    @Mock
    private PasswordEncoder passwordEncoder;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(trainerDao, trainingTypeDao, usernameGenerator, gymMetrics, passwordEncoder);
    }

    private Trainer buildTrainer(String username, boolean active) {
        var user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setActive(active);
        user.setRole(Role.ROLE_TRAINER);

        var trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        return trainer;
    }

    @Test
    void createProfile_shouldEncodePasswordAndReturnRawInResult() {
        var specialization = new TrainingType(5L, "YOGA");

        when(usernameGenerator.generate("Jane", "Doe")).thenReturn("Jane.Doe");
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        when(trainingTypeDao.findById(5L)).thenReturn(Optional.of(specialization));

        var result = trainerService.createProfile("Jane", "Doe", 5L);

        assertEquals(10, result.originalPassword().length());
        assertEquals("$2a$10$encoded", result.trainer().getUser().getPassword());
        assertEquals(Role.ROLE_TRAINER, result.trainer().getUser().getRole());
        verify(passwordEncoder).encode(result.originalPassword());
        verify(trainerDao).create(result.trainer());
        verify(gymMetrics).incrementTrainerRegistrations();
    }

    @Test
    void createProfile_shouldThrow_whenSpecializationNotFound() {
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.createProfile("Jane", "Doe", 99L));
        verifyNoInteractions(trainerDao);
        verifyNoInteractions(gymMetrics);
    }

    @Test
    void createProfile_shouldThrow_whenNameMissing() {
        assertThrows(ValidationException.class, () -> trainerService.createProfile("", "Doe", 5L));
        verifyNoInteractions(trainingTypeDao);
        verifyNoInteractions(gymMetrics);
    }

    @Test
    void updateProfileAndStatus_shouldUpdateNameAndKeepSpecializationReadOnly() {
        var originalSpecialization = new TrainingType(5L, "YOGA");
        var trainer = buildTrainer("Jane.Doe", true);
        trainer.setSpecialization(originalSpecialization);
        when(trainerDao.findByUsernameWithProfile("Jane.Doe")).thenReturn(Optional.of(trainer));

        var result = trainerService.updateProfileAndStatus("Jane.Doe", "Janet",
                "Doey", false);

        assertEquals("Janet", result.getUser().getFirstName());
        assertEquals("Doey", result.getUser().getLastName());
        assertEquals(originalSpecialization, result.getSpecialization());
        assertFalse(result.getUser().isActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void updateProfileAndStatus_shouldThrow_whenNameMissing() {
        var trainer = buildTrainer("Jane.Doe", true);
        when(trainerDao.findByUsernameWithProfile("Jane.Doe")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class,
                () -> trainerService.updateProfileAndStatus("Jane.Doe", " ",
                        "Doey", true));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void setActive_shouldDeactivate_whenCurrentlyActive() {
        var trainer = buildTrainer("Jane.Doe", true);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));

        trainerService.setActive("Jane.Doe", false);

        assertFalse(trainer.getUser().isActive());
    }

    @Test
    void setActive_shouldThrow_whenCurrentlyActive() {
        var trainer = buildTrainer("John.Smith", true);
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class, () -> trainerService.setActive("John.Smith", true));
        verify(trainerDao, never()).update(trainer);
    }

    @Test
    void getTrainersNotAssigned_shouldDelegateToDao() {
        when(trainerDao.findNotAssignedToTrainee("John.Smith")).thenReturn(List.of(new Trainer()));

        assertEquals(1, trainerService.getTrainersNotAssignedToTrainee("John.Smith").size());
    }

    @Test
    void getProfile_shouldReturnProfile_whenFound() {
        var trainer = buildTrainer("Jane.Doe", true);
        when(trainerDao.findByUsernameWithProfile("Jane.Doe")).thenReturn(Optional.of(trainer));

        assertEquals(trainer, trainerService.getProfile("Jane.Doe"));
    }

    @Test
    void getProfile_shouldThrow_whenNotFound() {
        when(trainerDao.findByUsernameWithProfile("Unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.getProfile("Unknown"));
    }

    @Test
    void changePassword_shouldEncodeNewPassword() {
        var trainer = buildTrainer("Jane.Doe", true);
        when(trainerDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.encode("newPwd1234")).thenReturn("$2a$10$newEncoded");

        trainerService.changePassword("Jane.Doe", "newPwd1234");

        assertEquals("$2a$10$newEncoded", trainer.getUser().getPassword());
        verify(trainerDao).update(trainer);
    }
}
