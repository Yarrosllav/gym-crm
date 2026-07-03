package com.gym.dao.impl;

import com.gym.dao.TraineeDAO;
import com.gym.db.Storage;
import com.gym.model.Trainee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Repository
public class TraineeDAOImpl implements TraineeDAO {

    private final Storage storage;

    @Override
    public void create(Trainee trainee) {
        log.debug("Saving Trainee with username: {}", trainee.getUsername());
        if (trainee.getUserId() == null) {
            trainee.setUserId(UUID.randomUUID());
            storage.saveTrainee(trainee.getUserId(), trainee);
            log.debug("Trainee saved successfully with id: {}", trainee.getUserId());
        }else {
            log.debug("Trainee already exists with id: {}", trainee.getUserId());
        }
    }

    @Override
    public Trainee findById(UUID id) {
        log.debug("Fetching Trainee with id: {}", id);
        return storage.getAllTrainees().get(id);
    }

    @Override
    public Collection<Trainee> findAll() {
        log.debug("Fetching all Trainees");
        return storage.getAllTrainees().values();
    }

    @Override
    public void delete(UUID id) {
        log.debug("Deleting Trainee with id: {}", id);
        storage.removeTrainee(id);
    }

    @Override
    public void update(Trainee trainee) {
        log.debug("Updating Trainee with username: {}", trainee.getUsername());

        if (!storage.getAllTrainees().containsKey(trainee.getUserId())) {
            throw new IllegalArgumentException("Trainee not found");
        }

        storage.saveTrainee(trainee.getUserId(), trainee);
        log.debug("Trainee updated successfully with id: {}", trainee.getUserId());
    }
}
