package com.gym.report.dto;

import java.util.List;

public record YearSummary(int year, List<MonthSummary> months) {
}
