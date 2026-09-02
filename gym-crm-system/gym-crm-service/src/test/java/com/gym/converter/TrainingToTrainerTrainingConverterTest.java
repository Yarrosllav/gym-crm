package com.gym.converter;

import com.gym.model.Trainee;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import com.gym.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingToTrainerTrainingConverterTest {

    private final TrainingToTrainerTrainingConverter converter = new TrainingToTrainerTrainingConverter();

    @Test
    void convert_shouldMapTraineeFullNameAndTrainingDetails() {
        var traineeUser = new User();
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Smith");
        var trainee = new Trainee();
        trainee.setUser(traineeUser);

        var training = new Training();
        training.setTrainee(trainee);
        training.setTrainingName("Evening Yoga");
        training.setTrainingDate(LocalDate.of(2026, 6, 2));
        training.setTrainingType(new TrainingType(2L, "Yoga"));
        training.setTrainingDuration(45);

        var result = converter.convert(training);

        assertEquals("Evening Yoga", result.trainingName());
        assertEquals(LocalDate.of(2026, 6, 2), result.trainingDate());
        assertEquals("Yoga", result.trainingType());
        assertEquals(45, result.trainingDuration());
        assertEquals("John Smith", result.traineeName());
    }
}
