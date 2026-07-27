package com.gym.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TrainersListUpdateRequest(
        @NotEmpty(message = "Trainers list is required") List<String> trainerUsernames
) {
}
