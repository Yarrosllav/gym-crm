package com.gym.service.impl;

import com.gym.dao.IReadOnlyDao;
import com.gym.dao.ITrainerDao;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.service.AbstractProfileService;
import com.gym.service.PasswordGenerator;
import com.gym.service.UsernameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TrainerService extends AbstractProfileService<Trainer, Long> {

    private final ITrainerDao trainerDao;

    private final UsernameGenerator usernameGenerator;

    private final IReadOnlyDao<TrainingType, Long> trainingTypeDao;

    public TrainerService(ITrainerDao trainerDao, IReadOnlyDao<TrainingType, Long> trainingTypeDao,
                          UsernameGenerator usernameGenerator) {
        super(trainerDao);
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    protected Optional<Trainer> findByUsername(String username) {
        log.info("Fetching Trainer by username: {}", username);
        return trainerDao.findByUsername(username);
    }

    @Transactional
    public Trainer createProfile(String firstName, String lastName, Long specializationId) {
        log.info("Creating Trainer profile with first name: {}, last name: {}", firstName, lastName);

        validateTrainer(firstName, lastName, specializationId);

        var specialization = trainingTypeDao.findById(specializationId)
                .orElseThrow(() -> {
                    log.warn("Create Trainer profile rejected: training type not found, id={}", specializationId);
                    return new EntityNotFoundException("Training type not found: " + specializationId);
                });

        var user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(usernameGenerator.generate(firstName, lastName));
        user.setPassword(PasswordGenerator.generate());
        user.setActive(true);

        var trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        create(trainer);
        log.info("Created Trainer profile with username: {}", user.getUsername());
        return trainer;
    }

    @Transactional
    public Trainer updateProfile(String username, String password, Trainer updated) {
        log.info("Updating Trainer profile with username: {}", username);

        var trainer = authenticate(username, password);
        var updatedUser = updated.getUser();

        if (updatedUser.getFirstName() == null || updatedUser.getFirstName().isBlank()
                || updatedUser.getLastName() == null || updatedUser.getLastName().isBlank()) {
            log.warn("Update Trainer profile rejected: first/last name missing");
            throw new ValidationException("First name and last name are required");
        }

        trainer.getUser().setFirstName(updatedUser.getFirstName());
        trainer.getUser().setLastName(updatedUser.getLastName());
        trainer.setSpecialization(updated.getSpecialization());

        update(trainer);
        log.info("Updated Trainer profile: {}", username);
        return trainer;
    }

    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        log.info("Fetching Trainers not assigned to Trainee: {}", traineeUsername);
        return trainerDao.findNotAssignedToTrainee(traineeUsername);
    }

    private void validateTrainer(String firstName, String lastName, Long specializationId) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            log.warn("Create Trainer profile rejected: first/last name missing");
            throw new ValidationException("First name and last name are required");
        }
        if (specializationId == null) {
            log.warn("Create Trainer profile rejected: specialization missing");
            throw new ValidationException("Specialization is required");
        }
    }
}
