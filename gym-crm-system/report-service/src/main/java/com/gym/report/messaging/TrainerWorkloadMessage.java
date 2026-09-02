package com.gym.report.messaging;

import java.time.LocalDate;

public record TrainerWorkloadMessage(
        String trainerUsername, String firstName, String lastName, Boolean isActive,
        LocalDate trainingDate, Integer trainingDuration, ActionType actionType
) {
    public enum ActionType {ADD, DELETE}
}
