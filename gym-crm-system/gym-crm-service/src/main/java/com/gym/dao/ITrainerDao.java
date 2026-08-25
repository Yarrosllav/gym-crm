package com.gym.dao;

import com.gym.model.Trainer;

import java.util.List;

public interface ITrainerDao extends IProfileDao<Trainer, Long> {
    List<Trainer> findNotAssignedToTrainee(String traineeUsername);
}
