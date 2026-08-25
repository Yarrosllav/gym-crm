package com.gym.service;

import com.gym.dao.ITraineeDao;
import com.gym.dao.ITrainerDao;
import com.gym.dto.request.TrainerWorkloadRequest;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.metrics.GymMetrics;
import com.gym.model.*;
import com.gym.service.impl.TraineeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;
    @Mock
    private GymMetrics gymMetrics;
    @Mock
    private ReportIntegrationService reportIntegrationService;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee buildTrainee(String username, boolean active) {
        var user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setUsername(username);
        user.setActive(active);
        user.setRole(Role.ROLE_TRAINEE);

        var trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);
        return trainee;
    }

    private Trainer buildTrainer(String username, boolean active) {
        var user = new User();
        user.setFirstName("Mike");
        user.setLastName("Jones");
        user.setUsername(username);
        user.setActive(active);
        user.setRole(Role.ROLE_TRAINER);

        var trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUser(user);
        return trainer;
    }

    @Test
    void createProfile_shouldEncodePasswordAndReturnRawInResult() {
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");

        var result = traineeService.createProfile("John", "Smith",
                LocalDate.of(2000, 1, 1), "Main St");

        assertEquals(10, result.originalPassword().length());
        assertEquals("$2a$10$encoded", result.trainee().getUser().getPassword());
        assertEquals(Role.ROLE_TRAINEE, result.trainee().getUser().getRole());
        verify(passwordEncoder).encode(result.originalPassword());
        verify(traineeDao).create(result.trainee());
        verify(gymMetrics).incrementTraineeRegistrations();
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
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsernameWithProfile("John.Smith")).thenReturn(Optional.of(trainee));

        var result = traineeService.updateProfileAndStatus("John.Smith", "Johnny",
                "Smithy", LocalDate.of(1995, 5, 5),
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
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsernameWithProfile("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.updateProfileAndStatus("John.Smith", " ", "Smithy",
                        LocalDate.of(1995, 5, 5), "New Address", true));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void getProfile_shouldReturnProfile_whenFound() {
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsernameWithProfile("John.Smith")).thenReturn(Optional.of(trainee));

        assertEquals(trainee, traineeService.getProfile("John.Smith"));
    }

    @Test
    void getProfile_shouldThrow_whenNotFound() {
        when(traineeDao.findByUsernameWithProfile("Unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeService.getProfile("Unknown"));
    }

    @Test
    void changePassword_shouldEncodeNewPassword() {
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.encode("newPwd1234")).thenReturn("$2a$10$newEncoded");

        traineeService.changePassword("John.Smith", "newPwd1234");

        assertEquals("$2a$10$newEncoded", trainee.getUser().getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void changePassword_shouldThrow_whenNewPasswordBlank() {

        assertThrows(ValidationException.class,
                () -> traineeService.changePassword("John.Smith", " "));
    }

    @Test
    void setActive_shouldActivate_whenCurrentlyInactive() {
        var trainee = buildTrainee("John.Smith", false);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.setActive("John.Smith", true);

        assertTrue(trainee.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void setActive_shouldDeactivate_whenCurrentlyActive() {
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.setActive("John.Smith", false);

        assertFalse(trainee.getUser().isActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void setActive_shouldThrow_whenCurrentlyActive() {
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class, () -> traineeService.setActive("John.Smith", true));
        verify(traineeDao, never()).update(trainee);
    }

    @Test
    void deleteByUsername_shouldDeleteTrainee_whenAuthenticated() {
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsernameWithProfile("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername("John.Smith");

        verify(traineeDao).delete(trainee);
    }

    @Test
    void deleteByUsername_shouldNotifyReportServiceForEachCascadedTraining() {
        var trainee = buildTrainee("John.Smith", true);
        var training1 = new Training();
        training1.setTrainer(buildTrainer("Mike.Jones", true));
        training1.setTrainingDate(LocalDate.of(2026, 1, 10));
        training1.setTrainingDuration(60);
        trainee.setTrainings(List.of(training1));
        when(traineeDao.findByUsernameWithProfile("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername("John.Smith");

        verify(reportIntegrationService).notifyWorkload(
                eq("Mike.Jones"), any(), any(), anyBoolean(), eq(LocalDate.of(2026, 1, 10)), eq(60),
                eq(TrainerWorkloadRequest.ActionType.DELETE));
        verify(traineeDao).delete(trainee);
    }

    @Test
    void deleteByUsername_shouldThrow_whenProfileNotFound() {
        when(traineeDao.findByUsernameWithProfile("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeService.deleteByUsername("unknown"));
    }

    @Test
    void updateTrainersList_shouldReplaceTrainers_whenAuthenticated() {
        var trainee = buildTrainee("John.Smith", true);
        var trainer = buildTrainer("Mike.Jones", true);
        when(traineeDao.findByUsernameWithProfile("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Jones")).thenReturn(Optional.of(trainer));

        var result = traineeService.updateTrainersList("John.Smith", List.of("Mike.Jones"));

        assertEquals(Set.of(trainer), result);
        assertEquals(Set.of(trainer), trainee.getTrainers());
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateTrainersList_shouldThrow_whenTrainerNotFound() {
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findByUsernameWithProfile("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Jones")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainersList("John.Smith", List.of("Mike.Jones")));
    }

    @Test
    void findById_shouldDelegateToDao() {
        var trainee = buildTrainee("John.Smith", true);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.findById(1L).isPresent());
    }

    @Test
    void findAll_shouldDelegateToDao() {
        when(traineeDao.findAll()).thenReturn(List.of(buildTrainee("John.Smith", true)));

        assertEquals(1, traineeService.findAll().size());
    }
}
