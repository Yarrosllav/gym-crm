package com.gym.dao;

import com.gym.dao.impl.UserDao;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private NativeQuery<Boolean> query;

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        userDao = new UserDao(sessionFactory);
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenUserFound() {
        when(session.createNativeQuery(anyString(), eq(Boolean.class))).thenReturn(query);
        when(query.setParameter("username", "John.Smith")).thenReturn(query);

        when(query.uniqueResult()).thenReturn(true);

        assertTrue(userDao.existsByUsername("John.Smith"));
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenUserNotFound() {
        when(session.createNativeQuery(anyString(), eq(Boolean.class))).thenReturn(query);
        when(query.setParameter("username", "Unknown")).thenReturn(query);

        when(query.uniqueResult()).thenReturn(false);

        assertFalse(userDao.existsByUsername("Unknown"));
    }
}
