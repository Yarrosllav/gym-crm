package com.gym.service;

import com.gym.client.ReportServiceClient;
import com.gym.dto.request.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportIntegrationService {

    private final ReportServiceClient reportServiceClient;

    @CircuitBreaker(name = "reportService", fallbackMethod = "fallback")
    public void notifyWorkload(String trainerUsername, String firstName, String lastName, boolean active,
                               LocalDate trainingDate, int trainingDuration,
                               TrainerWorkloadRequest.ActionType actionType) {
        log.info("Calling report service for trainer={}", trainerUsername);
        reportServiceClient.applyWorkload(new TrainerWorkloadRequest(
                trainerUsername, firstName, lastName, active, trainingDate, trainingDuration, actionType));
    }

    void fallback(String trainerUsername, String firstName, String lastName, boolean active,
                          LocalDate trainingDate, int trainingDuration,
                          TrainerWorkloadRequest.ActionType actionType, Throwable ex) {
        log.warn("Report service unavailable, workload update skipped for trainer={}: {}",
                trainerUsername, ex.getMessage());
    }
}
