package com.gym.report.dao;

import com.gym.report.model.TrainerTrainingSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TrainerTrainingSummaryRepository extends MongoRepository<TrainerTrainingSummary, String> {
    Optional<TrainerTrainingSummary> findByTrainerUsername(String trainerUsername);
}
