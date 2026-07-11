package com.gym.service.impl;

import com.gym.dao.TrainingDAO;
import com.gym.model.Training;
import com.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainingServiceImpl implements TrainingService {

    private final TrainingDAO trainingDAO;

    @Override
    public void create(Training training) {
        log.info("Creating training for trainee with id {} and trainer with id {}", training.getTraineeId(), training.getTrainerId());

        trainingDAO.create(training);
        log.info("Training created successfully with id {}", training.getTrainingId());
    }

    @Override
    public Training findById(UUID id) {
        log.info("Fetching Training with ID: {}", id);
        return trainingDAO.findById(id);
    }
}
