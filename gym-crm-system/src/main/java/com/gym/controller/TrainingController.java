package com.gym.controller;

import com.gym.dto.request.AddTrainingRequest;
import com.gym.service.impl.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training")
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINER') and #request.trainerUsername() == authentication.name)")
    @Operation(summary = "Add a new training (no update/delete via REST)")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        trainingService.addTraining(
                request.traineeUsername(), request.trainerUsername(), request.trainingName(),
                request.trainingDate(), request.trainingDuration());
        return ResponseEntity.ok().build();
    }
}
