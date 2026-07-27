package com.gym.converter;

import com.gym.dto.response.TraineeSummaryResponse;
import com.gym.model.Trainee;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TraineeToSummaryConverter implements Converter<Trainee, TraineeSummaryResponse> {
    @Override
    public TraineeSummaryResponse convert(Trainee source) {
        return new TraineeSummaryResponse(
                source.getUser().getUsername(),
                source.getUser().getFirstName(),
                source.getUser().getLastName());
    }
}
