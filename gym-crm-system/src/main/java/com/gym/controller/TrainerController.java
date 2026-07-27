package com.gym.controller;

import com.gym.converter.TrainerToProfileConverter;
import com.gym.converter.TrainerToRegistrationResponseConverter;
import com.gym.converter.TrainingToTrainerTrainingConverter;
import com.gym.dto.request.ActivateRequest;
import com.gym.dto.request.TrainerRegistrationRequest;
import com.gym.dto.request.TrainerUpdateRequest;
import com.gym.dto.response.RegistrationResponse;
import com.gym.dto.response.TrainerProfileResponse;
import com.gym.dto.response.TrainerTrainingResponse;
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
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    private final TrainerToRegistrationResponseConverter registrationConverter;
    private final TrainerToProfileConverter profileConverter;
    private final TrainingToTrainerTrainingConverter trainingConverter;

    @PostMapping
    @Operation(summary = "Register a new trainer")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        var trainer = trainerService.createProfile(
                request.firstName(), request.lastName(), request.specializationId());
        return ResponseEntity.ok(registrationConverter.convert(trainer));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @PathVariable String username,
            @RequestHeader("Password") String password) {
        var trainer = trainerService.getProfile(username, password);
        return ResponseEntity.ok(profileConverter.convert(trainer));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update trainer profile (specialization is read-only)")
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @Valid @RequestBody TrainerUpdateRequest request) {

        var trainer = trainerService.updateProfileAndStatus(username, password, request.firstName(),
                request.lastName(), request.isActive());
        return ResponseEntity.ok(profileConverter.convert(trainer));
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainer")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @Valid @RequestBody ActivateRequest request) {
        trainerService.setActive(username, password, request.isActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer's trainings list filtered by criteria")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestHeader("Password") String password,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {
        trainerService.authenticate(username, password);
        var trainings = trainingService.getTrainerTrainings(username, periodFrom, periodTo, traineeName);
        return ResponseEntity.ok(trainings.stream().map(trainingConverter::convert).toList());
    }
}
