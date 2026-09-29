package com.gym.cucumber.component;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public TestDatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanAll() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE trainings");
        jdbcTemplate.execute("TRUNCATE TABLE trainee_trainer");
        jdbcTemplate.execute("TRUNCATE TABLE trainees");
        jdbcTemplate.execute("TRUNCATE TABLE trainers");
        jdbcTemplate.execute("DELETE FROM users WHERE username <> 'admin'");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }
}
