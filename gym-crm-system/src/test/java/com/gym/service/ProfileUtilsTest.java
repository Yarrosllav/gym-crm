package com.gym.service;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProfileUtilsTest {

    private Trainee dummyTrainee;

    @BeforeEach
    void setUp() {
        dummyTrainee = new Trainee();
        dummyTrainee.setFirstName("John");
        dummyTrainee.setLastName("Doe");
        dummyTrainee.setUsername("John.Doe");
    }

    @Test
    void testGenerateUsername_WhenUnique_ShouldReturnBaseName() {
        var emptyTrainees = List.<Trainee>of();
        var emptyTrainers = List.<Trainer>of();

        var result = ProfileUtils.generateUsername("Bruce", "Wayne", emptyTrainees, emptyTrainers);

        assertEquals("Bruce.Wayne", result);
    }

    @Test
    void testGenerateUsername_WhenAlreadyExists_ShouldAddSerialNumber() {
        var trainees = List.of(dummyTrainee);
        var emptyTrainers = List.<Trainer>of();

        var result = ProfileUtils.generateUsername("John", "Doe", trainees, emptyTrainers);

        assertEquals("John.Doe1", result);
    }

    @Test
    void testGeneratePassword_ShouldReturnTenChars() {
        var password = ProfileUtils.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }
}
