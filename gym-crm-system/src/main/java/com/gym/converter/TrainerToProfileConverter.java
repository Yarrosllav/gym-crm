package com.gym.converter;

import com.gym.dto.response.TrainerProfileResponse;
import com.gym.model.Trainer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainerToProfileConverter implements Converter<Trainer, TrainerProfileResponse> {

    private final TraineeToSummaryConverter traineeToSummaryConverter;

    @Override
    public TrainerProfileResponse convert(Trainer source) {
        var trainees = source.getTrainees().stream()
                .map(traineeToSummaryConverter::convert)
                .toList();
        return new TrainerProfileResponse(
                source.getUser().getUsername(),
                source.getUser().getFirstName(),
                source.getUser().getLastName(),
                source.getSpecialization().getTrainingTypeName(),
                source.getUser().isActive(),
                trainees);
    }
}
