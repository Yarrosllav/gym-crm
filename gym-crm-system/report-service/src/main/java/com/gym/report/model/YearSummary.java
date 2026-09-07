package com.gym.report.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class YearSummary {
    private int year;
    private List<MonthSummary> months = new ArrayList<>();
}
