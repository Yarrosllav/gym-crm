package com.gym.report.controller;

import com.gym.report.dto.TrainerWorkloadRequest;
import com.gym.report.dto.TrainerWorkloadSummaryResponse;
import com.gym.report.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer-workloads")
@RequiredArgsConstructor
@Tag(name = "TrainerWorkload")
public class WorkloadController {

    private final WorkloadService workloadService;

    @PostMapping
    @Operation(summary = "Apply a training workload change (ADD or DELETE) for a trainer")
    public ResponseEntity<Void> applyWorkload(@Valid @RequestBody TrainerWorkloadRequest request){
        workloadService.applyWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{trainerUsername}")
    @Operation(summary = "Get monthly training-hours summary for a trainer")
    public ResponseEntity<TrainerWorkloadSummaryResponse> getSummary(@PathVariable String trainerUsername) {
        return ResponseEntity.ok(workloadService.getSummary(trainerUsername));
    }
}
