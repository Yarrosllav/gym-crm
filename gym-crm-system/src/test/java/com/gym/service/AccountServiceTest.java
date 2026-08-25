package com.gym.service;

import com.gym.dao.IUserDao;
import com.gym.exception.AuthenticationException;
import com.gym.exception.ValidationException;
import com.gym.metrics.GymMetrics;
import com.gym.model.Role;
import com.gym.model.User;
import com.gym.security.BruteForceProtector;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import com.gym.service.impl.AccountService;
import com.gym.service.impl.TraineeService;
import com.gym.service.impl.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private IUserDao userDao;
    @Mock
    private BruteForceProtector bruteForceProtector;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private GymMetrics gymMetrics;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(traineeService, trainerService, userDao, authenticationManager,
                bruteForceProtector, jwtService, tokenBlacklistService, gymMetrics);
    }

    private User buildUser(Role role) {
        var user = new User();
        user.setUsername("John.Smith");
        user.setRole(role);
        return user;
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        when(bruteForceProtector.isBlocked("John.Smith")).thenReturn(false);
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(buildUser(Role.ROLE_TRAINEE)));
        when(jwtService.generateToken("John.Smith", "ROLE_TRAINEE")).thenReturn("jwt-token");

        var token = accountService.login("John.Smith", "pwd");

        assertEquals("jwt-token", token);
        verify(bruteForceProtector).onSuccess("John.Smith");
        verify(gymMetrics).incrementLoginSuccess();
    }

    @Test
    void login_shouldThrow_whenAlreadyBlocked() {
        when(bruteForceProtector.isBlocked("John.Smith")).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> accountService.login("John.Smith", "pwd"));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void login_shouldRegisterFailure_whenCredentialsInvalid() {
        when(bruteForceProtector.isBlocked("John.Smith")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(AuthenticationException.class, () -> accountService.login("John.Smith", "wrong"));
        verify(bruteForceProtector).onFailure("John.Smith");
        verify(gymMetrics).incrementLoginFailure();
    }

    @Test
    void logout_shouldBlacklistToken() {
        when(jwtService.extractJti("abc.def.ghi")).thenReturn("jti-123");
        when(jwtService.extractExpiration("abc.def.ghi")).thenReturn(new Date(System.currentTimeMillis() + 60000));

        accountService.logout("Bearer abc.def.ghi");

        verify(tokenBlacklistService).blacklist(eq("jti-123"), any(Instant.class));
    }

    @Test
    void logout_shouldThrow_whenHeaderMissingBearerPrefix() {
        assertThrows(ValidationException.class, () -> accountService.logout("abc.def.ghi"));
    }

    @Test
    void changePassword_shouldDelegateToTraineeService_whenRoleIsTrainee() {
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(buildUser(Role.ROLE_TRAINEE)));

        accountService.changePassword("John.Smith", "old", "newPwd1234");

        verify(authenticationManager).authenticate(any());
        verify(traineeService).changePassword("John.Smith", "newPwd1234");
        verifyNoInteractions(trainerService);
    }

    @Test
    void changePassword_shouldDelegateToTrainerService_whenRoleIsTrainer() {
        when(userDao.findByUsername("Jane.Doe")).thenReturn(Optional.of(buildUser(Role.ROLE_TRAINER)));

        accountService.changePassword("Jane.Doe", "old", "newPwd1234");

        verify(trainerService).changePassword("Jane.Doe", "newPwd1234");
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordInvalid() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(AuthenticationException.class,
                () -> accountService.changePassword("John.Smith", "wrong", "newPwd1234"));
        verifyNoInteractions(traineeService, trainerService);
    }

    @Test
    void changePassword_shouldThrow_whenRoleIsAdmin() {
        when(userDao.findByUsername("admin")).thenReturn(Optional.of(buildUser(Role.ROLE_ADMIN)));

        assertThrows(ValidationException.class, () -> accountService.changePassword("admin", "old", "newPwd1234"));
    }
}
