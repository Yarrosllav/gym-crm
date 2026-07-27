package com.gym.service.impl;

import com.gym.dao.ITraineeDao;
import com.gym.dao.ITrainerDao;
import com.gym.dao.ITrainingDao;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Training;
import com.gym.service.AbstractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class TrainingService extends AbstractService<Training, Long> {

    private final ITraineeDao traineeDao;

    private final ITrainerDao trainerDao;

    private final ITrainingDao trainingDao;

    public TrainingService(ITrainingDao trainingDao, ITraineeDao traineeDao, ITrainerDao trainerDao) {
        super(trainingDao);
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername, String trainingName,
                                LocalDate trainingDate, Integer trainingDuration) {
        log.info("Adding Training for Trainee {} with Trainer {} and Training name {}",
                traineeUsername, trainerUsername, trainingName);

        validateTraining(trainingName, trainingDate, trainingDuration);

        var trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> {
                    log.warn("Add training rejected: trainee not found, username={}", traineeUsername);
                    return new EntityNotFoundException("Trainee not found: " + traineeUsername);
                });
        var trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> {
                    log.warn("Add training rejected: trainer not found, username={}", trainerUsername);
                    return new EntityNotFoundException("Trainer not found: " + trainerUsername);
                });

        var training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(trainingDuration);

        create(training);
        log.info("Added training '{}' for trainee {} with trainer {}", trainingName, traineeUsername, trainerUsername);
        return training;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingTypeName) {
        log.info("Fetching Trainings for Trainee: {} with criteria (fromDate: {}, toDate : {}, trainerName: {})",
                traineeUsername, fromDate, toDate, trainerName);
        return trainingDao.findByTraineeCriteria(traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        log.info("Fetching Trainings for Trainer: {} with criteria (fromDate: {}, toDate : {}, traineeName: {})",
                trainerUsername, fromDate, toDate, traineeName);
        return trainingDao.findByTrainerCriteria(trainerUsername, fromDate, toDate, traineeName);
    }

    private void validateTraining(String trainingName, LocalDate trainingDate, Integer trainingDuration) {
        if (trainingName == null || trainingName.isBlank()) {
            log.warn("Add Training rejected: name missing");
            throw new ValidationException("Training name is required");
        }
        if (trainingDate == null || trainingDuration == null) {
            log.warn("Add Training rejected: training date or duration missing");
            throw new ValidationException("Training date and duration are required");
        }
    }
}
