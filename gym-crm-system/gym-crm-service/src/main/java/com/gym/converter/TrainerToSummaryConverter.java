package com.gym.converter;

import com.gym.dto.response.TrainerSummaryResponse;
import com.gym.model.Trainer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TrainerToSummaryConverter implements Converter<Trainer, TrainerSummaryResponse> {
    @Override
    public TrainerSummaryResponse convert(Trainer source) {
        return new TrainerSummaryResponse(
                source.getUser().getUsername(),
                source.getUser().getFirstName(),
                source.getUser().getLastName(),
                source.getSpecialization().getTrainingTypeName());
    }
}
