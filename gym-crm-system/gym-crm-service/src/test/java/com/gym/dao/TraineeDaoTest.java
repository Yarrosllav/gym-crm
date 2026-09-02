package com.gym.dao;

import com.gym.dao.impl.TraineeDao;
import com.gym.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeDaoTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Query<Trainee> query;

    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        traineeDao = new TraineeDao(sessionFactory);
    }

    @Test
    void findByUsername_shouldReturnTrainee_whenFound() {
        var trainee = new Trainee();
        when(session.createQuery("from Trainee t where t.user.username = :username", Trainee.class))
                .thenReturn(query);
        when(query.setParameter("username", "John.Smith")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        var result = traineeDao.findByUsername("John.Smith");

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(session.createQuery("from Trainee t where t.user.username = :username", Trainee.class))
                .thenReturn(query);
        when(query.setParameter("username", "unknown")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        assertTrue(traineeDao.findByUsername("unknown").isEmpty());
    }
}
