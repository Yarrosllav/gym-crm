package com.gym.config;

import com.gym.db.Storage;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@ComponentScan("com.gym")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public Map<UUID, Training> trainingMap() {
        return new HashMap<>();
    }

    @Bean
    public Map<UUID, Trainee> traineeMap() {
        return new HashMap<>();
    }

    @Bean
    public Map<UUID, Trainer> trainerMap() {
        return new HashMap<>();
    }

    @Bean
    public Storage storage(Map<UUID, Training> trainingMap,
                           Map<UUID, Trainee> traineeMap,
                           Map<UUID, Trainer> trainerMap) {
        return new Storage(traineeMap, trainingMap, trainerMap);
    }
}
