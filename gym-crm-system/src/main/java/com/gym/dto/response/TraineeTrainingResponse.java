package com.gym.dto.response;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName, LocalDate trainingDate, String trainingType,
        Integer trainingDuration, String trainerName
) {
}
