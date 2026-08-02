package com.gym.service.impl;

import com.gym.dao.IReadOnlyDao;
import com.gym.model.TrainingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingTypeService {

    private final IReadOnlyDao<TrainingType, Long> trainingTypeDao;

    public List<TrainingType> getAll() {
        log.info("Fetching all training types");
        return trainingTypeDao.findAll();
    }
}
