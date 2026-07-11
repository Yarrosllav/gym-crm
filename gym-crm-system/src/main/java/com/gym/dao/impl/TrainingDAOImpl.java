package com.gym.dao.impl;

import com.gym.dao.TrainingDAO;
import com.gym.db.Storage;
import com.gym.model.Training;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Repository
public class TrainingDAOImpl implements TrainingDAO {

    private final Storage storage;

    @Override
    public void create(Training training) {
        log.debug("Saving Training with name: {}", training.getTrainingName());
        if (training.getTrainingId() == null) {
            training.setTrainingId(UUID.randomUUID());
            storage.saveTraining(training.getTrainingId(), training);
            log.debug("Training saved successfully with id: {}", training.getTrainingId());
        } else {
            log.debug("Training already exists with id: {}", training.getTrainingId());
        }
    }

    @Override
    public Collection<Training> findAll() {
        log.debug("Fetching all Trainings");
        return storage.getAllTrainings().values();
    }

    @Override
    public Training findById(UUID id) {
        log.debug("Fetching Training with id: {}", id);
        return storage.getAllTrainings().get(id);
    }
}
