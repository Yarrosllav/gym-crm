package com.gym.service;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        var password = new StringBuilder(PASSWORD_LENGTH);
        for (var i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}
