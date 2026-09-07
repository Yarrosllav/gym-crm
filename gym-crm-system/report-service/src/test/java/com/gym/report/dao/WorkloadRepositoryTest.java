package com.gym.report.dao;

import com.gym.report.model.TrainerMonthlyWorkload;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Query<TrainerMonthlyWorkload> query;

    private WorkloadRepository workloadRepository;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        workloadRepository = new WorkloadRepository(sessionFactory);
    }

    @Test
    void create_shouldPersistEntity() {
        var workload = new TrainerMonthlyWorkload();

        workloadRepository.create(workload);

        verify(session).persist(workload);
    }

    @Test
    void update_shouldMergeEntity() {
        var workload = new TrainerMonthlyWorkload();

        workloadRepository.update(workload);

        verify(session).merge(workload);
    }

    @Test
    void find_shouldReturnEntity_whenFound() {
        var workload = new TrainerMonthlyWorkload();
        when(session.createQuery(anyString(), eq(TrainerMonthlyWorkload.class))).thenReturn(query);
        when(query.setParameter(eq("username"), any())).thenReturn(query);
        when(query.setParameter(eq("year"), any())).thenReturn(query);
        when(query.setParameter(eq("month"), any())).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(workload));

        var result = workloadRepository.find("Jane.Doe", 2026, 3);

        assertTrue(result.isPresent());
        assertEquals(workload, result.get());
    }

    @Test
    void find_shouldReturnEmpty_whenNotFound() {
        when(session.createQuery(anyString(), eq(TrainerMonthlyWorkload.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        assertTrue(workloadRepository.find("Unknown", 2026, 3).isEmpty());
    }

    @Test
    void findAllByUsername_shouldReturnAllRecords() {
        var workload = new TrainerMonthlyWorkload();
        when(session.createQuery(anyString(), eq(TrainerMonthlyWorkload.class))).thenReturn(query);
        when(query.setParameter(eq("username"), any())).thenReturn(query);
        when(query.list()).thenReturn(List.of(workload));

        var result = workloadRepository.findAllByUsername("Jane.Doe");

        assertEquals(1, result.size());
    }
}
