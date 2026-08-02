package com.gym.service;

import com.gym.exception.AuthenticationException;
import com.gym.service.impl.AccountService;
import com.gym.service.impl.TraineeService;
import com.gym.service.impl.TrainerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void login_shouldSucceed_whenTraineeCredentialsMatch() {
        when(traineeService.matchCredentials("John.Smith", "pwd")).thenReturn(true);

        accountService.login("John.Smith", "pwd");

        verify(trainerService, never()).matchCredentials(any(), any());
    }

    @Test
    void login_shouldSucceed_whenTrainerCredentialsMatch() {
        when(traineeService.matchCredentials("Jane.Doe", "pwd")).thenReturn(false);
        when(trainerService.matchCredentials("Jane.Doe", "pwd")).thenReturn(true);

        accountService.login("Jane.Doe", "pwd");
    }

    @Test
    void login_shouldThrow_whenNoProfileMatches() {
        when(traineeService.matchCredentials("unknown", "pwd")).thenReturn(false);
        when(trainerService.matchCredentials("unknown", "pwd")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> accountService.login("unknown", "pwd"));
    }

    @Test
    void changePassword_shouldDelegateToTraineeService_whenTraineeCredentialsMatch() {
        when(traineeService.matchCredentials("John.Smith", "old")).thenReturn(true);

        accountService.changePassword("John.Smith", "old", "newPassword1");

        verify(traineeService).changePassword("John.Smith", "old", "newPassword1");
        verifyNoInteractions(trainerService);
    }

    @Test
    void changePassword_shouldDelegateToTrainerService_whenTrainerCredentialsMatch() {
        when(traineeService.matchCredentials("Jane.Doe", "old")).thenReturn(false);
        when(trainerService.matchCredentials("Jane.Doe", "old")).thenReturn(true);

        accountService.changePassword("Jane.Doe", "old", "newPassword1");

        verify(trainerService).changePassword("Jane.Doe", "old", "newPassword1");
    }

    @Test
    void changePassword_shouldThrow_whenNoProfileMatches() {
        when(traineeService.matchCredentials("unknown", "old")).thenReturn(false);
        when(trainerService.matchCredentials("unknown", "old")).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> accountService.changePassword("unknown", "old", "newPassword1"));
    }
}
