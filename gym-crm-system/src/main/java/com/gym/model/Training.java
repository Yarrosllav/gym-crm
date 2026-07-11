package com.gym.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Training {

    private UUID traineeId;

    private UUID trainerId;

    private String trainingName;

    private TrainingType trainingType;

    private LocalDate trainingDate;

    private Double trainingDuration;

    private UUID trainingId;
}
