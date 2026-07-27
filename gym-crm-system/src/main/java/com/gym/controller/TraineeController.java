package com.gym.controller;

import com.gym.converter.TraineeToProfileConverter;
import com.gym.converter.TraineeToRegistrationResponseConverter;
import com.gym.converter.TrainerToSummaryConverter;
import com.gym.converter.TrainingToTraineeTrainingConverter;
import com.gym.dto.request.ActivateRequest;
import com.gym.dto.request.TraineeRegistrationRequest;
import com.gym.dto.request.TraineeUpdateRequest;
import com.gym.dto.request.TrainersListUpdateRequest;
import com.gym.dto.response.RegistrationResponse;
import com.gym.dto.response.TraineeProfileResponse;
import com.gym.dto.response.TraineeTrainingResponse;
import com.gym.dto.response.TrainerSummaryResponse;
import com.gym.service.impl.TraineeService;
import com.gym.service.impl.TrainerService;
import com.gym.service.impl.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    private final TraineeToRegistrationResponseConverter registrationConverter;
    private final TraineeToProfileConverter profileConverter;
    private final TrainerToSummaryConverter trainerSummaryConverter;
    private final TrainingToTraineeTrainingConverter trainingConverter;

    @PostMapping
    @Operation(summary = "Register a new trainee")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        var trainee = traineeService.createProfile(
                request.firstName(), request.lastName(), request.dateOfBirth(), request.address());
        return ResponseEntity.ok(registrationConverter.convert(trainee));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @PathVariable String username,
            @RequestHeader("Password") String password) {
        var trainee = traineeService.getProfile(username, password);
        return ResponseEntity.ok(profileConverter.convert(trainee));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateProfile(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @Valid @RequestBody TraineeUpdateRequest request) {

        var trainee = traineeService.updateProfileAndStatus(username, password, request.firstName(),
                request.lastName(), request.dateOfBirth(), request.address(), request.isActive());
        return ResponseEntity.ok(profileConverter.convert(trainee));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile (cascades trainings)")
    public ResponseEntity<Void> delete(
            @PathVariable String username,
            @RequestHeader("Password") String password) {
        traineeService.deleteByUsername(username, password);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainee")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @Valid @RequestBody ActivateRequest request) {
        traineeService.setActive(username, password, request.isActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Get active trainers not assigned to this trainee")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedActiveTrainers(
            @PathVariable String username,
            @RequestHeader("Password") String password) {
        traineeService.authenticate(username, password);
        var trainers = trainerService.getTrainersNotAssignedToTrainee(username);
        return ResponseEntity.ok(trainers.stream().map(trainerSummaryConverter::convert).toList());
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's trainers list")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTrainersList(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @Valid @RequestBody TrainersListUpdateRequest request) {
        var trainers = traineeService.updateTrainersList(username, password, request.trainerUsernames());
        return ResponseEntity.ok(trainers.stream().map(trainerSummaryConverter::convert).toList());
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee's trainings list filtered by criteria")
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        traineeService.authenticate(username, password);
        var trainings = trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType);
        return ResponseEntity.ok(trainings.stream().map(trainingConverter::convert).toList());
    }
}
