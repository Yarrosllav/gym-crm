package com.gym.dao;

import com.gym.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface ITrainingDao extends ICreateAndReadDao<Training, Long> {
    List<Training> findByTraineeCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                         String trainerName, String trainingTypeName);

    List<Training> findByTrainerCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                         String traineeName);
}
