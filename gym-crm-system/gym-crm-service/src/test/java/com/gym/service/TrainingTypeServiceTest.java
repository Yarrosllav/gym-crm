package com.gym.service;

import com.gym.dao.IReadOnlyDao;
import com.gym.model.TrainingType;
import com.gym.service.impl.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private IReadOnlyDao<TrainingType, Long> trainingTypeDao;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void getAll_shouldDelegateToDao() {
        var types = List.of(new TrainingType(1L, "Cardio"), new TrainingType(2L, "Yoga"));
        when(trainingTypeDao.findAll()).thenReturn(types);

        assertEquals(types, trainingTypeService.getAll());
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoTypesExist() {
        when(trainingTypeDao.findAll()).thenReturn(List.of());

        assertEquals(0, trainingTypeService.getAll().size());
    }
}
