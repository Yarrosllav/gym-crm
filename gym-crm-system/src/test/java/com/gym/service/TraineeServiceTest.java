package com.gym.service;

import com.gym.dao.ITraineeDao;
import com.gym.dao.ITrainerDao;
import com.gym.exception.AuthenticationException;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.metrics.GymMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.service.impl.TraineeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private ITraineeDao traineeDao;
    @Mock
    private ITrainerDao trainerDao;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private GymMetrics gymMetrics;

    @InjectMocks
    private TraineeService traineeService;

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
    void createProfile_shouldPersistTraineeWithGeneratedCredentials() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");

        var result = traineeService.createProfile("John", "Smith",
                LocalDate.of(2000, 1, 1), "Main St");

        assertEquals("John.Smith", result.getUser().getUsername());
        assertTrue(result.getUser().isActive());

        assertNotNull(result.getUser().getPassword());
        assertEquals(10, result.getUser().getPassword().length());

        verify(gymMetrics).incrementTraineeRegistrations();
        verify(traineeDao).create(result);
    }

    @Test
    void createProfile_shouldThrow_whenFirstNameMissing() {
        assertThrows(ValidationException.class,
                () -> traineeService.createProfile(" ", "Smith", null, null));
        verifyNoInteractions(traineeDao);
        verifyNoInteractions(gymMetrics);
    }

    @Test
    void updateProfileAndStatus_shouldUpdateFieldsAndActiveStatus() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        var result = traineeService.updateProfileAndStatus("John.Smith", "pwd",
                "Johnny", "Smithy", LocalDate.of(1995, 5, 5),
                "New Address", false);

        assertEquals("Johnny", result.getUser().getFirstName());
        assertEquals("Smithy", result.getUser().getLastName());
        assertEquals(LocalDate.of(1995, 5, 5), result.getDateOfBirth());
        assertEquals("New Address", result.getAddress());
        assertFalse(result.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateProfileAndStatus_shouldThrow_whenFirstNameMissing() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.updateProfileAndStatus("John.Smith", "pwd",
                        " ", "Smithy", LocalDate.of(1995, 5, 5),
                        "New Address", true));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void updateProfileAndStatus_shouldThrow_whenPasswordWrong() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class,
                () -> traineeService.updateProfileAndStatus("John.Smith", "wrong",
                        "Johnny", "Smithy", LocalDate.of(1995, 5, 5),
                        "New Address", true));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void matchCredentials_shouldReturnTrue_whenPasswordMatches() {
        var trainee = buildTrainee("John.Smith", "secret", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.matchCredentials("John.Smith", "secret"));
    }

    @Test
    void matchCredentials_shouldReturnFalse_whenPasswordDoesNotMatch() {
        var trainee = buildTrainee("John.Smith", "secret", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertFalse(traineeService.matchCredentials("John.Smith", "wrong"));
    }

    @Test
    void matchCredentials_shouldReturnFalse_whenUserNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertFalse(traineeService.matchCredentials("unknown", "any"));
    }

    @Test
    void getProfile_shouldReturnProfile_whenAuthenticated() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertEquals(trainee, traineeService.getProfile("John.Smith", "pwd"));
    }

    @Test
    void getProfile_shouldThrow_whenPasswordWrong() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class, () -> traineeService.getProfile("John.Smith", "wrong"));
    }

    @Test
    void changePassword_shouldUpdatePassword_whenAuthenticated() {
        var trainee = buildTrainee("John.Smith", "old", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.changePassword("John.Smith", "old", "newPassword1");

        assertEquals("newPassword1", trainee.getUser().getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordWrong() {
        var trainee = buildTrainee("John.Smith", "old", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class,
                () -> traineeService.changePassword("John.Smith", "wrong", "newPassword1"));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void changePassword_shouldThrow_whenNewPasswordBlank() {
        var trainee = buildTrainee("John.Smith", "old", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.changePassword("John.Smith", "old", " "));
    }

    @Test
    void setActive_shouldActivate_whenCurrentlyInactive() {
        var trainee = buildTrainee("John.Smith", "pwd", false);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.setActive("John.Smith", "pwd", true);

        assertTrue(trainee.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void setActive_shouldDeactivate_whenCurrentlyActive() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.setActive("John.Smith", "pwd", false);

        assertFalse(trainee.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void setActive_shouldThrow_whenCurrentlyActive() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class, () -> traineeService.setActive("John.Smith", "pwd", true));
        verify(traineeDao, never()).update(trainee);
    }

    @Test
    void deleteByUsername_shouldDeleteTrainee_whenAuthenticated() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername("John.Smith", "pwd");

        verify(traineeDao).delete(trainee);
    }

    @Test
    void deleteByUsername_shouldThrow_whenProfileNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> traineeService.deleteByUsername("unknown", "pwd"));
    }

    @Test
    void updateTrainersList_shouldReplaceTrainers_whenAuthenticated() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        var trainer = buildTrainer("Mike.Jones", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Jones")).thenReturn(Optional.of(trainer));

        var result = traineeService.updateTrainersList("John.Smith", "pwd", List.of("Mike.Jones"));

        assertEquals(Set.of(trainer), result);
        assertEquals(Set.of(trainer), trainee.getTrainers());
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateTrainersList_shouldThrow_whenTrainerNotFound() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Jones")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainersList("John.Smith", "pwd", List.of("Mike.Jones")));
    }

    @Test
    void findById_shouldDelegateToDao() {
        var trainee = buildTrainee("John.Smith", "pwd", true);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.findById(1L).isPresent());
    }

    @Test
    void findAll_shouldDelegateToDao() {
        when(traineeDao.findAll()).thenReturn(List.of(buildTrainee("John.Smith", "pwd", true)));

        assertEquals(1, traineeService.findAll().size());
    }
}
