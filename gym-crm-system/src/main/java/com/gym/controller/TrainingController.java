package com.gym.controller;

import com.gym.dto.request.AddTrainingRequest;
import com.gym.service.impl.TrainerService;
import com.gym.service.impl.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training")
public class TrainingController {

    private final TrainingService trainingService;

    private final TrainerService trainerService;

    @PostMapping
    @Operation(summary = "Add a new training (no update/delete via REST)")
    public ResponseEntity<Void> addTraining(
            @RequestHeader("Password") String password,
            @Valid @RequestBody AddTrainingRequest request) {
        trainerService.authenticate(request.trainerUsername(), password);
        trainingService.addTraining(
                request.traineeUsername(), request.trainerUsername(), request.trainingName(),
                request.trainingDate(), request.trainingDuration());
        return ResponseEntity.ok().build();
    }
}
