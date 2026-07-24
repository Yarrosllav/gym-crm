package com.gym.dao.impl;

import com.gym.dao.AbstractDao;
import com.gym.dao.ITrainingDao;
import com.gym.model.Training;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
public class TrainingDao extends AbstractDao<Training, Long> implements ITrainingDao {

    public TrainingDao(SessionFactory sessionFactory) {
        super(sessionFactory, Training.class);
    }

    @Override
    public List<Training> findByTraineeCriteria(String traineeUsername,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                String trainerName,
                                                String trainingTypeName) {
        log.debug("Fetching Trainings for Trainee: {} with criteria", traineeUsername);

        var hql = new StringBuilder("""
                select tr from Training tr
                where tr.trainee.user.username = :traineeUsername
                """);
        if (fromDate != null) hql.append(" and tr.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" and tr.trainingDate <= :toDate");
        if (trainerName != null)
            hql.append(" and concat(tr.trainer.user.firstName, ' ', tr.trainer.user.lastName) like :trainerName");
        if (trainingTypeName != null) hql.append(" and tr.trainingType.trainingTypeName = :trainingTypeName");

        var query = session().createQuery(hql.toString(), Training.class);
        query.setParameter("traineeUsername", traineeUsername);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null) query.setParameter("trainerName", "%" + trainerName + "%");
        if (trainingTypeName != null) query.setParameter("trainingTypeName", trainingTypeName);

        return query.list();
    }

    @Override
    public List<Training> findByTrainerCriteria(String trainerUsername,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                String traineeName) {
        log.debug("Fetching Trainings for Trainer: {} with criteria", trainerUsername);

        var hql = new StringBuilder("""
                select tr from Training tr
                where tr.trainer.user.username = :trainerUsername
                """);
        if (fromDate != null) hql.append(" and tr.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" and tr.trainingDate <= :toDate");
        if (traineeName != null)
            hql.append(" and concat(tr.trainee.user.firstName, ' ', tr.trainee.user.lastName) like :traineeName");

        var query = session().createQuery(hql.toString(), Training.class);
        query.setParameter("trainerUsername", trainerUsername);
        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null) query.setParameter("traineeName", "%" + traineeName + "%");

        return query.list();
    }
}
