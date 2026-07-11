package com.gym.service;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;

import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;

public class ProfileUtils {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final Random RANDOM = new Random();

    public static String generateUsername(String firstName,
                                          String lastName,
                                          Collection<Trainee> trainees,
                                          Collection<Trainer> trainers) {
        var baseUsername = firstName + "." + lastName;
        var finalUsername = baseUsername;
        var counter = 1;

        while (isUsernameTaken(finalUsername, trainees, trainers)) {
            finalUsername = baseUsername + counter;
            counter++;
        }

        return finalUsername;
    }

    private static boolean isUsernameTaken(String username,
                                           Collection<Trainee> trainees,
                                           Collection<Trainer> trainers) {

        Predicate<User> matches = user -> user.getUsername() != null && user.getUsername().equals(username);

        var existsInTrainees = trainees.stream().anyMatch(matches);
        var existsInTrainers = trainers.stream().anyMatch(matches);

        return existsInTrainees || existsInTrainers;
    }

    public static String generatePassword() {
        var password = new StringBuilder();
        for (var i = 0; i < 10; i++) {
            password.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return password.toString();
    }
}
