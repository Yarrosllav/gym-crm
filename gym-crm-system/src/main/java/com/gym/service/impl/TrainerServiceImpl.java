package com.gym.service.impl;

import com.gym.dao.TraineeDAO;
import com.gym.dao.TrainerDAO;
import com.gym.model.Trainer;
import com.gym.service.ProfileUtils;
import com.gym.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerServiceImpl implements TrainerService {

    private final TrainerDAO trainerDAO;

    private final TraineeDAO traineeDAO;

    @Override
    public void create(Trainer trainer) {
        log.info("Creating profile for trainer: {} {}", trainer.getFirstName(), trainer.getLastName());

        var username = ProfileUtils.generateUsername(
                trainer.getFirstName(),
                trainer.getLastName(),
                traineeDAO.findAll(),
                trainerDAO.findAll()
        );
        var password = ProfileUtils.generatePassword();

        trainer.setUsername(username);
        trainer.setPassword(password);

        trainerDAO.create(trainer);

        log.info("Trainer profile created successfully. Username: {}", username);
    }

    @Override
    public void update(Trainer trainer) {
        log.info("Updating Trainer profile with ID: {} ", trainer.getUserId());
        trainerDAO.update(trainer);
    }

    @Override
    public Trainer findById(UUID id) {
        log.info("Fetching Trainer profile with ID: {}", id);
        return trainerDAO.findById(id);
    }
}
