package com.gym.service.impl;

import com.gym.dao.IUserDao;
import com.gym.exception.AuthenticationException;
import com.gym.exception.ValidationException;
import com.gym.metrics.GymMetrics;
import com.gym.model.Role;
import com.gym.security.BruteForceProtector;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final IUserDao userDao;
    private final AuthenticationManager authenticationManager;
    private final BruteForceProtector bruteForceProtector;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final GymMetrics gymMetrics;

    @Transactional(readOnly = true)
    public String login(String username, String password) {
        log.info("Logging in: {}", username);

        if (bruteForceProtector.isBlocked(username)) {
            log.warn("Login blocked for '{}' due to too many failed attempts", username);
            throw new AuthenticationException("Account temporarily locked. Try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            bruteForceProtector.onFailure(username);
            gymMetrics.incrementLoginFailure();
            log.warn("Login failed for '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }

        bruteForceProtector.onSuccess(username);
        gymMetrics.incrementLoginSuccess();
        var role = userDao.findByUsername(username).orElseThrow().getRole().name();

        log.info("Login successful for '{}'", username);
        return jwtService.generateToken(username, role);
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ValidationException("Missing or invalid Authorization header");
        }
        var token = authorizationHeader.substring(7);
        var jti = jwtService.extractJti(token);
        var expiry = jwtService.extractExpiration(token).toInstant();
        tokenBlacklistService.blacklist(jti, expiry);
        log.info("Token invalidated (logout)");
    }

    @Transactional()
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for: {}", username);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            log.warn("Change password failed: invalid old password for '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }

        var role = userDao.findByUsername(username).orElseThrow().getRole();
        if (role == Role.ROLE_TRAINEE) {
            traineeService.changePassword(username, newPassword);
        } else if (role == Role.ROLE_TRAINER) {
            trainerService.changePassword(username, newPassword);
        } else {
            throw new ValidationException("Not a trainee or trainer username");
        }
    }
}
