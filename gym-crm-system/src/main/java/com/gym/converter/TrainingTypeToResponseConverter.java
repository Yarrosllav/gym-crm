package com.gym.converter;

import com.gym.dto.response.TrainingTypeResponse;
import com.gym.model.TrainingType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeToResponseConverter implements Converter<TrainingType, TrainingTypeResponse> {
    @Override
    public TrainingTypeResponse convert(TrainingType source) {
        return new TrainingTypeResponse(source.getId(), source.getTrainingTypeName());
    }
}
