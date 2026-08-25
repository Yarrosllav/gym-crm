package com.gym.service;

import com.gym.client.ReportServiceClient;
import com.gym.dto.request.TrainerWorkloadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportIntegrationServiceTest {

    @Mock
    private ReportServiceClient reportServiceClient;

    private ReportIntegrationService reportIntegrationService;

    @BeforeEach
    void setUp() {
        reportIntegrationService = new ReportIntegrationService(reportServiceClient);
    }

    @Test
    void notifyWorkload_shouldCallClient_whenServiceAvailable() {
        reportIntegrationService.notifyWorkload("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadRequest.ActionType.ADD);

        verify(reportServiceClient).applyWorkload(new TrainerWorkloadRequest(
                "Jane.Doe", "Jane", "Doe", true, LocalDate.of(2026, 3, 15), 60,
                TrainerWorkloadRequest.ActionType.ADD));
    }
}
