package com.gym.dao;

import com.gym.model.Trainee;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractDaoTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Query<Trainee> query;

    private AbstractDao<Trainee, Long> dao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        dao = new AbstractDao<>(sessionFactory, Trainee.class) {
        };
    }

    @Test
    void create_shouldPersistEntity() {
        var trainee = new Trainee();
        dao.create(trainee);
        verify(session).persist(trainee);
    }

    @Test
    void update_shouldMergeEntity() {
        var trainee = new Trainee();
        dao.update(trainee);
        verify(session).merge(trainee);
    }

    @Test
    void delete_shouldRemoveEntity() {
        var trainee = new Trainee();
        dao.delete(trainee);
        verify(session).remove(trainee);
    }

    @Test
    void findById_shouldReturnEntity_whenFound() {
        var trainee = new Trainee();
        trainee.setId(1L);
        when(session.get(Trainee.class, 1L)).thenReturn(trainee);

        var result = dao.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(session.get(Trainee.class, 1L)).thenReturn(null);

        assertTrue(dao.findById(1L).isEmpty());
    }

    @Test
    void findAll_shouldReturnAllEntities() {
        var trainee = new Trainee();
        when(session.createQuery("from Trainee", Trainee.class)).thenReturn(query);
        when(query.list()).thenReturn(List.of(trainee));

        var result = dao.findAll();

        assertEquals(1, result.size());
        assertEquals(trainee, result.getFirst());
    }
}
