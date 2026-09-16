package com.gym.cucumber.component;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminTestSeeder {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "Admin@12345";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public AdminTestSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public void ensureKnownAdminPassword() {
        var hash = passwordEncoder.encode(ADMIN_PASSWORD);
        var updated = jdbcTemplate.update(
                "UPDATE users SET password = ? WHERE username = ?", hash, ADMIN_USERNAME);

        if (updated == 0) {
            throw new IllegalStateException(
                    "Admin user not found — check that V4__seed_admin_user.sql ran successfully");
        }
    }
}
