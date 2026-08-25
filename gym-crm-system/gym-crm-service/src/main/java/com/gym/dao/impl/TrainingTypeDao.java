package com.gym.dao.impl;

import com.gym.dao.AbstractDao;
import com.gym.dao.IReadOnlyDao;
import com.gym.model.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingTypeDao extends AbstractDao<TrainingType, Long> implements IReadOnlyDao<TrainingType, Long> {

    public TrainingTypeDao(SessionFactory sessionFactory) {
        super(sessionFactory, TrainingType.class);
    }
}
