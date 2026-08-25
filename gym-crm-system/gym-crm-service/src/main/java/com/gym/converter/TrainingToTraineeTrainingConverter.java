package com.gym.converter;

import com.gym.dto.response.TraineeTrainingResponse;
import com.gym.model.Training;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TrainingToTraineeTrainingConverter implements Converter<Training, TraineeTrainingResponse> {
    @Override
    public TraineeTrainingResponse convert(Training source) {
        var trainerUser = source.getTrainer().getUser();
        return new TraineeTrainingResponse(
                source.getTrainingName(),
                source.getTrainingDate(),
                source.getTrainingType().getTrainingTypeName(),
                source.getTrainingDuration(),
                trainerUser.getFirstName() + " " + trainerUser.getLastName());
    }
}
