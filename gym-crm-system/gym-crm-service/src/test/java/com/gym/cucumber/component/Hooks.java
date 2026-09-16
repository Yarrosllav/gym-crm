package com.gym.cucumber.component;

import com.gym.security.BruteForceProtector;
import io.cucumber.java.Before;

public class Hooks {

    private final TestDatabaseCleaner cleaner;
    private final AdminTestSeeder adminSeeder;
    private final BruteForceProtector bruteForceProtector;

    public Hooks(TestDatabaseCleaner cleaner, AdminTestSeeder adminSeeder,
                 BruteForceProtector bruteForceProtector) {
        this.cleaner = cleaner;
        this.adminSeeder = adminSeeder;
        this.bruteForceProtector = bruteForceProtector;
    }

    @Before
    public void setUpBeforeScenario() {
        cleaner.cleanAll();
        adminSeeder.ensureKnownAdminPassword();
        bruteForceProtector.resetAll();
    }
}
