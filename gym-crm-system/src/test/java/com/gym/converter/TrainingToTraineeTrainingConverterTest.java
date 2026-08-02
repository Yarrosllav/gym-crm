package com.gym.converter;

import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import com.gym.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingToTraineeTrainingConverterTest {

    private final TrainingToTraineeTrainingConverter converter = new TrainingToTraineeTrainingConverter();

    @Test
    void convert_shouldMapTrainerFullNameAndTrainingDetails() {
        var trainerUser = new User();
        trainerUser.setFirstName("Mike");
        trainerUser.setLastName("Jones");
        var trainer = new Trainer();
        trainer.setUser(trainerUser);

        var training = new Training();
        training.setTrainer(trainer);
        training.setTrainingName("Morning Run");
        training.setTrainingDate(LocalDate.of(2026, 6, 1));
        training.setTrainingType(new TrainingType(1L, "Cardio"));
        training.setTrainingDuration(60);

        var result = converter.convert(training);

        assertEquals("Morning Run", result.trainingName());
        assertEquals(LocalDate.of(2026, 6, 1), result.trainingDate());
        assertEquals("Cardio", result.trainingType());
        assertEquals(60, result.trainingDuration());
        assertEquals("Mike Jones", result.trainerName());
    }
}
