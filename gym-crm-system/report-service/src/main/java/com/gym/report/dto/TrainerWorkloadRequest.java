package com.gym.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
        @NotBlank(message = "Trainer username is required") String trainerUsername,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotNull(message = "Is Active is required") Boolean isActive,
        @NotNull(message = "Training date is required") LocalDate trainingDate,
        @NotNull(message = "Training duration is required") Integer trainingDuration,
        @NotNull(message = "Action type is required") ActionType actionType
) {
}
