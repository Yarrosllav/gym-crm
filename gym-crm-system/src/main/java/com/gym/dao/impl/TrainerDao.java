package com.gym.dao.impl;

import com.gym.dao.AbstractDao;
import com.gym.dao.ITrainerDao;
import com.gym.model.Trainer;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class TrainerDao extends AbstractDao<Trainer, Long> implements ITrainerDao {

    public TrainerDao(SessionFactory sessionFactory) {
        super(sessionFactory, Trainer.class);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Fetching Trainer by username: {}", username);
        return session()
                .createQuery("from Trainer t where t.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();

    }

    @Override
    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        log.debug("Fetching Trainers not assigned to Trainee: {}", traineeUsername);
        return session()
                .createQuery("""
                        select tr from Trainer tr
                        where tr not in (
                            select t from Trainee tn join tn.trainers t
                            where tn.user.username = :username
                        )
                        """, Trainer.class)
                .setParameter("username", traineeUsername)
                .list();
    }
}
