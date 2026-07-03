package com.gym.dao.impl;

import com.gym.dao.TrainerDAO;
import com.gym.db.Storage;
import com.gym.model.Trainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Repository
public class TrainerDAOImpl implements TrainerDAO {

    private final Storage storage;

    @Override
    public void create(Trainer trainer) {
        log.debug("Saving Trainer into map with username: {}", trainer.getUsername());
        if (trainer.getUserId() == null) {
            trainer.setUserId(UUID.randomUUID());
            storage.saveTrainer(trainer.getUserId(), trainer);
            log.debug("Trainer saved successfully with id: {}", trainer.getUserId());
        }else {
            log.debug("Trainer already exists with id: {}", trainer.getUserId());
        }
    }

    @Override
    public void update(Trainer trainer) {
        log.debug("Updating Trainer with username: {}", trainer.getUsername());

        if (!storage.getAllTrainers().containsKey(trainer.getUserId())) {
            throw new IllegalArgumentException("Trainer not found");
        }

        storage.saveTrainer(trainer.getUserId(), trainer);
        log.debug("Trainer updated successfully with id: {}", trainer.getUserId());
    }

    @Override
    public Trainer findById(UUID id) {
        log.debug("Fetching Trainer with id: {}", id);
        return storage.getAllTrainers().get(id);
    }

    @Override
    public Collection<Trainer> findAll() {
        log.debug("Fetching all Trainers");
        return storage.getAllTrainers().values();
    }
}
