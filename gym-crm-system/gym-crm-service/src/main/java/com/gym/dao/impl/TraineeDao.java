package com.gym.dao.impl;

import com.gym.dao.AbstractDao;
import com.gym.dao.ITraineeDao;
import com.gym.model.Trainee;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class TraineeDao extends AbstractDao<Trainee, Long> implements ITraineeDao {

    public TraineeDao(SessionFactory sessionFactory) {
        super(sessionFactory, Trainee.class);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        log.debug("Fetching Trainee by username: {}", username);
        return session()
                .createQuery("from Trainee t where t.user.username = :username", Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Override
    public Optional<Trainee> findByUsernameWithProfile(String username) {
        log.debug("Fetching Trainee with profile graph by username: {}", username);
        return session()
                .createQuery("""
                        select distinct t from Trainee t
                        left join fetch t.user
                        left join fetch t.trainers tr
                        left join fetch tr.user
                        left join fetch tr.specialization
                        where t.user.username = :username
                        """, Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}
