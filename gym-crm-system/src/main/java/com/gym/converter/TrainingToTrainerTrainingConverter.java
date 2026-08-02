package com.gym.converter;

import com.gym.dto.response.TrainerTrainingResponse;
import com.gym.model.Training;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TrainingToTrainerTrainingConverter implements Converter<Training, TrainerTrainingResponse> {
    @Override
    public TrainerTrainingResponse convert(Training source) {
        var traineeUser = source.getTrainee().getUser();
        return new TrainerTrainingResponse(
                source.getTrainingName(),
                source.getTrainingDate(),
                source.getTrainingType().getTrainingTypeName(),
                source.getTrainingDuration(),
                traineeUser.getFirstName() + " " + traineeUser.getLastName());
    }
}
