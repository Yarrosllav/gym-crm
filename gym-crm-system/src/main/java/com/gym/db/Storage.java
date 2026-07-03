package com.gym.db;

import com.gym.config.InjectData;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Setter
@InjectData
public class Storage {

    private final Map<UUID, Trainee> trainees;

    private final Map<UUID, Training> trainings;

    private final Map<UUID, Trainer> trainers;

    public Map<UUID, Trainee> getAllTrainees() {
        return Collections.unmodifiableMap(trainees);
    }

    public Map<UUID, Training> getAllTrainings() {
        return Collections.unmodifiableMap(trainings);
    }

    public Map<UUID, Trainer> getAllTrainers() {
        return Collections.unmodifiableMap(trainers);
    }

    public void saveTrainee(UUID id, Trainee trainee) {
        trainees.put(id, trainee);
    }

    public void saveTrainer(UUID id, Trainer trainer) {
        trainers.put(id, trainer);
    }

    public void saveTraining(UUID id, Training training) {
        trainings.put(id, training);
    }

    public void removeTrainee(UUID id) {
        trainees.remove(id);
    }
}
