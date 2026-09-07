package com.gym.report.controller;

import com.gym.report.dto.TrainerWorkloadSummaryResponse;
import com.gym.report.service.TrainerSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer-workloads")
@RequiredArgsConstructor
@Tag(name = "TrainerWorkload")
public class WorkloadController {

    private final TrainerSummaryService trainerSummaryService;

    @GetMapping("/{trainerUsername}")
    @Operation(summary = "Get monthly training-hours summary for a trainer")
    public ResponseEntity<TrainerWorkloadSummaryResponse> getSummary(@PathVariable String trainerUsername) {
        return ResponseEntity.ok(trainerSummaryService.getSummary(trainerUsername));
    }
}
