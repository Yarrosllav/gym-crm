package com.gym.service;

import com.gym.dao.TraineeDAO;
import com.gym.dao.TrainerDAO;
import com.gym.model.Trainee;
import com.gym.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceTest {

    @Mock
    private TraineeDAO traineeDAO;

    @Mock
    private TrainerDAO trainerDAO;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setFirstName("Jackie");
        trainee.setLastName("Chan");
    }

    @Test
    void testCreateProfile() {
        when(traineeDAO.findAll()).thenReturn(List.of());
        when(trainerDAO.findAll()).thenReturn(List.of());

        traineeService.create(trainee);

        assertEquals("Jackie.Chan", trainee.getUsername());

        assertNotNull(trainee.getPassword());
        assertEquals(10, trainee.getPassword().length());

        verify(traineeDAO, times(1)).create(trainee);
    }

    @Test
    void testGetProfile() {
        var id = UUID.randomUUID();
        when(traineeDAO.findById(id)).thenReturn(trainee);

        var result = traineeService.findById(id);

        assertEquals(trainee, result);
        verify(traineeDAO, times(1)).findById(id);
    }

    @Test
    void testDeleteProfile() {
        var id = UUID.randomUUID();
        traineeService.delete(id);
        verify(traineeDAO, times(1)).delete(id);
    }
}
