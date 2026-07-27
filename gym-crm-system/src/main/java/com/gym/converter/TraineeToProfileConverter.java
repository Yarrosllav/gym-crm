package com.gym.converter;

import com.gym.dto.response.TraineeProfileResponse;
import com.gym.model.Trainee;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TraineeToProfileConverter implements Converter<Trainee, TraineeProfileResponse> {

    private final TrainerToSummaryConverter trainerToSummaryConverter;

    @Override
    public TraineeProfileResponse convert(Trainee source) {
        var trainers = source.getTrainers().stream()
                .map(trainerToSummaryConverter::convert)
                .toList();
        return new TraineeProfileResponse(
                source.getUser().getUsername(),
                source.getUser().getFirstName(),
                source.getUser().getLastName(),
                source.getDateOfBirth(),
                source.getAddress(),
                source.getUser().isActive(),
                trainers);
    }
}
