package com.gym.health;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseHealthIndicatorTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private NativeQuery<Integer> query;

    @Test
    void health_shouldReturnUp_whenQuerySucceeds() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createNativeQuery("SELECT 1", Integer.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1);

        var health = new DatabaseHealthIndicator(sessionFactory).health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void health_shouldReturnDown_whenSessionThrows() {
        when(sessionFactory.openSession()).thenThrow(new RuntimeException("Connection refused"));

        var health = new DatabaseHealthIndicator(sessionFactory).health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
