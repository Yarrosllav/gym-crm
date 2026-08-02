package com.gym.controller;

import com.gym.converter.TrainingTypeToResponseConverter;
import com.gym.dto.response.TrainingTypeResponse;
import com.gym.service.impl.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@RequiredArgsConstructor
@Tag(name = "TrainingType")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final TrainingTypeToResponseConverter converter;

    @GetMapping
    @Operation(summary = "Get the fixed list of training types")
    public ResponseEntity<List<TrainingTypeResponse>> getAll() {
        var types = trainingTypeService.getAll();
        return ResponseEntity.ok(types.stream().map(converter::convert).toList());
    }
}
