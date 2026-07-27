package com.gym.converter;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TrainerToProfileConverterTest {

    private final TrainerToProfileConverter converter =
            new TrainerToProfileConverter(new TraineeToSummaryConverter());

    private User buildUser(String username, String firstName, String lastName, boolean active) {
        var user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(active);
        return user;
    }

    @Test
    void convert_shouldMapAllFieldsAndTrainees() {
        var trainee = new Trainee();
        trainee.setUser(buildUser("John.Smith", "John", "Smith", true));

        var trainer = new Trainer();
        trainer.setUser(buildUser("Jane.Doe", "Jane", "Doe", true));
        trainer.setSpecialization(new TrainingType(1L, "Yoga"));
        trainer.setTrainees(Set.of(trainee));

        var result = converter.convert(trainer);

        assertEquals("Jane.Doe", result.username());
        assertEquals("Jane", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("Yoga", result.specialization());
        assertTrue(result.isActive());
        assertEquals(1, result.trainees().size());
        assertEquals("John.Smith", result.trainees().get(0).username());
    }

    @Test
    void convert_shouldReturnEmptyTraineesList_whenNoTraineesAssigned() {
        var trainer = new Trainer();
        trainer.setUser(buildUser("Jane.Doe", "Jane", "Doe", true));
        trainer.setSpecialization(new TrainingType(1L, "Yoga"));

        var result = converter.convert(trainer);

        assertNotNull(result.trainees());
        assertTrue(result.trainees().isEmpty());
    }
}
