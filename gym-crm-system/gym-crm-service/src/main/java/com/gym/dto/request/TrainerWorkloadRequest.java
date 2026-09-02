package com.gym.dto.request;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
        String trainerUsername, String firstName, String lastName, boolean isActive,
        LocalDate trainingDate, Integer trainingDuration, ActionType actionType
) {
    public enum ActionType { ADD, DELETE }
}
