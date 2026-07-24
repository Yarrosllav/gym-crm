package com.gym.dao;

import com.gym.dao.impl.TrainingDao;
import com.gym.model.Training;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDaoTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Query<Training> query;

    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        trainingDao = new TrainingDao(sessionFactory);
    }

    @Test
    void findByTraineeCriteria_shouldApplyAllFilters_whenAllProvided() {
        var training = new Training();
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(training));

        var result = trainingDao.findByTraineeCriteria(
                "John.Smith", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Jane", "Cardio");

        assertEquals(1, result.size());
        verify(query).setParameter("traineeUsername", "John.Smith");
        verify(query).setParameter("fromDate", LocalDate.of(2026, 1, 1));
        verify(query).setParameter("trainerName", "%Jane%");
        verify(query).setParameter("trainingTypeName", "Cardio");
    }

    @Test
    void findByTraineeCriteria_shouldWorkWithoutOptionalFilters() {
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        var result = trainingDao.findByTraineeCriteria("John.Smith", null, null, null, null);

        assertTrue(result.isEmpty());
        verify(query, never()).setParameter(eq("fromDate"), any());
    }

    @Test
    void findByTrainerCriteria_shouldApplyAllFilters_whenAllProvided() {
        var training = new Training();
        when(session.createQuery(anyString(), eq(Training.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(training));

        var result = trainingDao.findByTrainerCriteria(
                "Jane.Doe", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "John");

        assertEquals(1, result.size());
        verify(query).setParameter("trainerUsername", "Jane.Doe");
        verify(query).setParameter("traineeName", "%John%");
    }
}
