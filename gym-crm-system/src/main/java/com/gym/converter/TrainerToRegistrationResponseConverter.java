package com.gym.converter;

import com.gym.dto.response.RegistrationResponse;
import com.gym.model.Trainer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TrainerToRegistrationResponseConverter implements Converter<Trainer, RegistrationResponse> {
    @Override
    public RegistrationResponse convert(Trainer source) {
        return new RegistrationResponse(source.getUser().getUsername(), source.getUser().getPassword());
    }
}
