package com.gym.service.impl;

import com.gym.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final TraineeService traineeService;

    private final TrainerService trainerService;

    public void login(String username, String password) {
        log.info("Logging in: {}", username);
        var isMatching = traineeService.matchCredentials(username, password)
                || trainerService.matchCredentials(username, password);
        if (!isMatching) {
            log.warn("Login failed for '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }
        log.info("Login successful for '{}'", username);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for: {}", username);
        if (traineeService.matchCredentials(username, oldPassword)) {
            traineeService.changePassword(username, oldPassword, newPassword);
            return;
        }
        if (trainerService.matchCredentials(username, oldPassword)) {
            trainerService.changePassword(username, oldPassword, newPassword);
            return;
        }
        log.warn("Change password failed: no matching profile for '{}'", username);
        throw new AuthenticationException("Invalid username or password");
    }
}
