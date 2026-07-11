package com.gym.service.impl;

import com.gym.dao.TraineeDAO;
import com.gym.dao.TrainerDAO;
import com.gym.model.Trainee;
import com.gym.service.ProfileUtils;
import com.gym.service.TraineeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TraineeServiceImpl implements TraineeService {

    private final TraineeDAO traineeDAO;

    private final TrainerDAO trainerDAO;

    @Override
    public void create(Trainee trainee) {
        log.info("Creating profile for trainee: {} {}", trainee.getFirstName(), trainee.getLastName());

        var username = ProfileUtils.generateUsername(
                trainee.getFirstName(),
                trainee.getLastName(),
                traineeDAO.findAll(),
                trainerDAO.findAll()
        );
        var password = ProfileUtils.generatePassword();

        trainee.setUsername(username);
        trainee.setPassword(password);

        traineeDAO.create(trainee);

        log.info("Trainee profile created successfully. Username: {}", username);
    }

    @Override
    public void update(Trainee trainee) {
        log.info("Updating Trainee profile with ID: {} ", trainee.getUserId());
        traineeDAO.update(trainee);
    }

    @Override
    public void delete(UUID id) {
        log.info("Deleting Trainee profile with ID: {}", id);
        traineeDAO.delete(id);
    }

    @Override
    public Trainee findById(UUID id) {
        log.info("Fetching Trainee profile with ID: {}", id);
        return traineeDAO.findById(id);
    }
}
