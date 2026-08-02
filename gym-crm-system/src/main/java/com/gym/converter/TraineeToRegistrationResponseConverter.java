package com.gym.converter;

import com.gym.dto.response.RegistrationResponse;
import com.gym.model.Trainee;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TraineeToRegistrationResponseConverter implements Converter<Trainee, RegistrationResponse> {
    @Override
    public RegistrationResponse convert(Trainee source) {
        return new RegistrationResponse(source.getUser().getUsername(), source.getUser().getPassword());
    }
}
