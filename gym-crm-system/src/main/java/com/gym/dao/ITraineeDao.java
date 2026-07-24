package com.gym.dao;

import com.gym.model.Trainee;

public interface ITraineeDao extends IProfileDao<Trainee, Long> {
    void delete(Trainee trainee);
}
