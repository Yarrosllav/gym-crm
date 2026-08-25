package com.gym.converter;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TraineeToProfileConverterTest {

    private final TraineeToProfileConverter converter =
            new TraineeToProfileConverter(new TrainerToSummaryConverter());

    private User buildUser(String username, String firstName, String lastName, boolean active) {
        var user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(active);
        return user;
    }

    @Test
    void convert_shouldMapAllFieldsAndTrainers() {
        var trainer = new Trainer();
        trainer.setUser(buildUser("Mike.Jones", "Mike", "Jones", true));
        trainer.setSpecialization(new TrainingType(1L, "Cardio"));

        var trainee = new Trainee();
        trainee.setUser(buildUser("John.Smith", "John", "Smith", true));
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Main St");
        trainee.setTrainers(Set.of(trainer));

        var result = converter.convert(trainee);

        assertEquals("John.Smith", result.username());
        assertEquals("John", result.firstName());
        assertEquals("Smith", result.lastName());
        assertEquals(LocalDate.of(2000, 1, 1), result.dateOfBirth());
        assertEquals("Main St", result.address());
        assertTrue(result.isActive());
        assertEquals(1, result.trainers().size());
        assertEquals("Mike.Jones", result.trainers().get(0).username());
        assertEquals("Cardio", result.trainers().get(0).specialization());
    }

    @Test
    void convert_shouldReturnEmptyTrainersList_whenNoTrainersAssigned() {
        var trainee = new Trainee();
        trainee.setUser(buildUser("John.Smith", "John", "Smith", true));

        var result = converter.convert(trainee);

        assertNotNull(result.trainers());
        assertTrue(result.trainers().isEmpty());
    }
}
