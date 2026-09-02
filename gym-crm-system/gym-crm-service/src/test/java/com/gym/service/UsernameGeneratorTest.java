package com.gym.service;

import com.gym.dao.IUserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private IUserDao userDao;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generate_shouldReturnBaseUsername_whenNoCollision() {
        when(userDao.existsByUsername("John.Smith")).thenReturn(false);

        assertEquals("John.Smith", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_shouldAppendSerialNumber_whenCollisionExists() {
        when(userDao.existsByUsername("John.Smith")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith1")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith2")).thenReturn(false);

        assertEquals("John.Smith2", usernameGenerator.generate("John", "Smith"));
    }
}
