package com.gym.config;

import com.gym.db.Storage;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
public class StorageInitPostProcessor implements BeanPostProcessor {

    @Value("${data.file.path}")
    private Resource dataFile;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(InjectData.class)) {
            log.info("Initializing bean with @InjectData: {}", beanName);

            if (bean instanceof Storage storage) {
                loadDataFromFile(storage);
            }

            log.info("Finished initializing bean: {}", beanName);
        }
        return bean;
    }

    private void loadDataFromFile(Storage storage) {
        log.info("Starting to load data from file: {}", dataFile.getFilename());

        if (!dataFile.exists()) {
            log.warn("Data file not found at path: {}", dataFile.getFilename());
            return;
        }

        try (var reader = new BufferedReader(new InputStreamReader(dataFile.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                createEntity(line, storage);
            }
            log.info("Successfully loaded {} trainees and {} trainers.",
                    storage.getAllTrainees().size(), storage.getAllTrainers().size());

        } catch (Exception e) {
            log.error("Failed to load data from file", e);
        }
    }

    private void createEntity(String line, Storage storage) {

        var parts = line.split(",");
        var entityType = parts[0].trim();

        switch (entityType) {
            case "TRAINEE" -> {
                if (parts.length >= 5) {
                    var trainee = new Trainee();
                    trainee.setUserId(UUID.randomUUID());
                    trainee.setFirstName(parts[1].trim());
                    trainee.setLastName(parts[2].trim());
                    trainee.setDateOfBirth(LocalDate.parse(parts[3].trim()));
                    trainee.setAddress(parts[4].trim());

                    storage.saveTrainee(trainee.getUserId(), trainee);
                }
            }

            case "TRAINER" -> {
                if (parts.length >= 4) {
                    var trainer = new Trainer();
                    trainer.setUserId(UUID.randomUUID());
                    trainer.setFirstName(parts[1].trim());
                    trainer.setLastName(parts[2].trim());
                    trainer.setSpecialization(parts[3].trim());

                    storage.saveTrainer(trainer.getUserId(), trainer);
                }
            }
            default -> log.warn("Unknown entity type in file: {}", entityType);
        }
    }
}
