package com.gym.service;

import com.gym.dao.TraineeDAO;
import com.gym.dao.TrainerDAO;
import com.gym.model.Trainer;
import com.gym.service.impl.TrainerServiceImpl;
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
class TrainerServiceTest {

    @Mock
    private TrainerDAO trainerDAO;

    @Mock
    private TraineeDAO traineeDAO;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        trainer = new Trainer();
        trainer.setFirstName("Jackie");
        trainer.setLastName("Chan");
    }

    @Test
    void testCreateProfile() {
        when(trainerDAO.findAll()).thenReturn(List.of());
        when(traineeDAO.findAll()).thenReturn(List.of());

        trainerService.create(trainer);

        assertEquals("Jackie.Chan", trainer.getUsername());

        assertNotNull(trainer.getPassword());
        assertEquals(10, trainer.getPassword().length());

        verify(trainerDAO, times(1)).create(trainer);
    }

    @Test
    void testGetProfile() {
        var id = UUID.randomUUID();
        when(trainerDAO.findById(id)).thenReturn(trainer);

        var result = trainerService.findById(id);

        assertEquals(trainer, result);
        verify(trainerDAO, times(1)).findById(id);
    }
}
