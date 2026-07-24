package com.gym.service.impl;

import com.gym.dao.ITraineeDao;
import com.gym.dao.ITrainerDao;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.service.AbstractProfileService;
import com.gym.service.PasswordGenerator;
import com.gym.service.UsernameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TraineeService extends AbstractProfileService<Trainee, Long> {

    private final ITraineeDao traineeDao;

    private final ITrainerDao trainerDao;

    private final UsernameGenerator usernameGenerator;

    public TraineeService(ITraineeDao traineeDao, ITrainerDao trainerDao,
                          UsernameGenerator usernameGenerator) {
        super(traineeDao);
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
    }

    @Override
    protected Optional<Trainee> findByUsername(String username) {
        log.debug("Fetching Trainee by username: {}", username);
        return traineeDao.findByUsername(username);
    }

    @Transactional
    public Trainee createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.info("Creating Trainee profile with first name: {}, last name: {}", firstName, lastName);

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            log.warn("Create Trainee profile rejected: first/last name missing");
            throw new ValidationException("First name and last name are required");
        }

        var user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(usernameGenerator.generate(firstName, lastName));
        user.setPassword(PasswordGenerator.generate());
        user.setActive(true);

        var trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        create(trainee);
        log.info("Created Trainee profile with username: {}", user.getUsername());
        return trainee;
    }

    @Transactional
    public Trainee updateProfile(String username, String password, Trainee updated) {
        log.info("Updating Trainee profile with username: {}", username);

        var trainee = authenticate(username, password);
        var updatedUser = updated.getUser();

        if (updatedUser.getFirstName() == null || updatedUser.getFirstName().isBlank()
                || updatedUser.getLastName() == null || updatedUser.getLastName().isBlank()) {
            log.warn("Update Trainee profile rejected: first/last name missing");
            throw new ValidationException("First name and last name are required");
        }

        trainee.getUser().setFirstName(updatedUser.getFirstName());
        trainee.getUser().setLastName(updatedUser.getLastName());
        trainee.setDateOfBirth(updated.getDateOfBirth());
        trainee.setAddress(updated.getAddress());

        update(trainee);
        log.info("Updated Trainee profile: {}", username);
        return trainee;
    }

    @Transactional
    public void deleteByUsername(String username, String password) {
        log.info("Deleting Trainee profile with username: {}", username);

        var trainee = authenticate(username, password);
        traineeDao.delete(trainee);
        log.info("Deleted Trainee profile and cascaded trainings: {}", username);
    }


    @Transactional
    public Set<Trainer> updateTrainersList(String username, String password, List<Long> trainerIds) {
        log.info("Updating trainers list for Trainee: {}", username);

        var trainee = authenticate(username, password);
        var trainers = trainerIds.stream()
                .map(id -> trainerDao.findById(id)
                        .orElseThrow(() -> {
                            log.warn("Update trainers list rejected: trainer not found, id={}", id);
                            return new EntityNotFoundException("Trainer not found: " + id);
                        }))
                .collect(Collectors.toSet());
        trainee.setTrainers(trainers);
        update(trainee);
        log.info("Updated trainers list for Trainee: {}", username);
        return trainers;
    }
}
