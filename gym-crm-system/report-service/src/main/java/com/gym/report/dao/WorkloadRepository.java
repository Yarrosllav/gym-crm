package com.gym.report.dao;

import com.gym.report.model.TrainerMonthlyWorkload;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkloadRepository {

    private final SessionFactory sessionFactory;

    private org.hibernate.Session session() {
        return sessionFactory.getCurrentSession();
    }

    public void create(TrainerMonthlyWorkload workload) {
        session().persist(workload);
    }

    public void update(TrainerMonthlyWorkload workload) {
        session().merge(workload);
    }

    public Optional<TrainerMonthlyWorkload> find(String username, int year, int month) {
        return session().createQuery(
                        "from TrainerMonthlyWorkload w where w.trainerUsername = :username and w.year = :year and w.month = :month",
                        TrainerMonthlyWorkload.class)
                .setParameter("username", username)
                .setParameter("year", year)
                .setParameter("month", month)
                .uniqueResultOptional();
    }

    public List<TrainerMonthlyWorkload> findAllByUsername(String username) {
        return session().createQuery(
                        "from TrainerMonthlyWorkload w where w.trainerUsername = :username", TrainerMonthlyWorkload.class)
                .setParameter("username", username)
                .list();
    }
}
