package com.gym.dao;

import com.gym.dao.impl.TrainerDao;
import com.gym.model.Trainer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerDaoTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Query<Trainer> query;

    private TrainerDao trainerDao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        trainerDao = new TrainerDao(sessionFactory);
    }

    @Test
    void findByUsername_shouldReturnTrainer_whenFound() {
        var trainer = new Trainer();
        when(session.createQuery("from Trainer t where t.user.username = :username", Trainer.class))
                .thenReturn(query);
        when(query.setParameter("username", "Jane.Doe")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainer));

        var result = trainerDao.findByUsername("Jane.Doe");

        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void findNotAssignedToTrainee_shouldReturnTrainersList() {
        var trainer = new Trainer();
        when(session.createQuery(anyString(), eq(Trainer.class))).thenReturn(query);
        when(query.setParameter(eq("username"), eq("John.Smith"))).thenReturn(query);
        when(query.list()).thenReturn(List.of(trainer));

        var result = trainerDao.findNotAssignedToTrainee("John.Smith");

        assertEquals(1, result.size());
        assertEquals(trainer, result.getFirst());
    }
}
