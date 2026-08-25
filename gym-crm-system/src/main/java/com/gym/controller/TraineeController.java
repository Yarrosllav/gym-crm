package com.gym.controller;

import com.gym.converter.TraineeToProfileConverter;
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
import com.gym.model.Role;
import com.gym.security.JwtService;
import com.gym.service.impl.TraineeService;
import com.gym.service.impl.TrainerService;
import com.gym.service.impl.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final TraineeToProfileConverter profileConverter;
    private final TrainerToSummaryConverter trainerSummaryConverter;
    private final TrainingToTraineeTrainingConverter trainingConverter;
    private final JwtService jwtService;

    @PostMapping
    @Operation(summary = "Register a new trainee")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        var trainee = traineeService.createProfile(
                request.firstName(), request.lastName(), request.dateOfBirth(), request.address());
        var token = jwtService.generateToken(trainee.trainee().getUser().getUsername(),
                Role.ROLE_TRAINEE.name());
        return ResponseEntity.ok(new RegistrationResponse(
                trainee.trainee().getUser().getUsername(), trainee.originalPassword(), token));
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINEE') and #username == authentication.name)")
    @Operation(summary = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) {
        var trainee = traineeService.getProfile(username);
        return ResponseEntity.ok(profileConverter.convert(trainee));
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINEE') and #username == authentication.name)")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request) {
        var trainee = traineeService.updateProfileAndStatus(username, request.firstName(),
                request.lastName(), request.dateOfBirth(), request.address(), request.isActive());
        return ResponseEntity.ok(profileConverter.convert(trainee));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINEE') and #username == authentication.name)")
    @Operation(summary = "Delete trainee profile (cascades trainings)")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        traineeService.deleteByUsername(username);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINEE') and #username == authentication.name)")
    @Operation(summary = "Activate or deactivate a trainee")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @Valid @RequestBody ActivateRequest request) {
        traineeService.setActive(username, request.isActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINEE') and #username == authentication.name)")
    @Operation(summary = "Get active trainers not assigned to this trainee")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedActiveTrainers(@PathVariable String username) {
        var trainers = trainerService.getTrainersNotAssignedToTrainee(username);
        return ResponseEntity.ok(trainers.stream().map(trainerSummaryConverter::convert).toList());
    }

    @PutMapping("/{username}/trainers")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINEE') and #username == authentication.name)")
    @Operation(summary = "Update trainee's trainers list")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTrainersList(
            @PathVariable String username,
            @Valid @RequestBody TrainersListUpdateRequest request) {
        var trainers = traineeService.updateTrainersList(username, request.trainerUsernames());
        return ResponseEntity.ok(trainers.stream().map(trainerSummaryConverter::convert).toList());
    }

    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINEE') and #username == authentication.name)")
    @Operation(summary = "Get trainee's trainings list filtered by criteria")
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        var trainings = trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType);
        return ResponseEntity.ok(trainings.stream().map(trainingConverter::convert).toList());
    }
}
