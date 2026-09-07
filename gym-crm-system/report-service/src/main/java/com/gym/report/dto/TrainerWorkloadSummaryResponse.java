package com.gym.report.dto;

import java.util.List;

public record TrainerWorkloadSummaryResponse(
        String trainerUsername, String firstName, String lastName, boolean isActive, List<YearSummaryResponse> years) {
}
