package com.gym.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.report.dto.ActionType;
import com.gym.report.dto.TrainerWorkloadRequest;
import com.gym.report.dto.TrainerWorkloadSummaryResponse;
import com.gym.report.exception.EntityNotFoundException;
import com.gym.report.security.ServiceJwtValidator;
import com.gym.report.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkloadController.class)
class WorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkloadService workloadService;
    @MockBean
    private ServiceJwtValidator serviceJwtValidator;

    @Test
    void applyWorkload_shouldReturn200_whenAuthorizedAndValid() throws Exception {
        when(serviceJwtValidator.isValid("valid-token")).thenReturn(true);
        var request = new TrainerWorkloadRequest("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, ActionType.ADD);

        mockMvc.perform(post("/api/trainer-workloads")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void applyWorkload_shouldReturn401_whenTokenMissing() throws Exception {
        var request = new TrainerWorkloadRequest("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, ActionType.ADD);

        mockMvc.perform(post("/api/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applyWorkload_shouldReturn401_whenTokenInvalid() throws Exception {
        when(serviceJwtValidator.isValid("bad-token")).thenReturn(false);
        var request = new TrainerWorkloadRequest("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, ActionType.ADD);

        mockMvc.perform(post("/api/trainer-workloads")
                        .header("Authorization", "Bearer bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void applyWorkload_shouldReturn400_whenTrainerUsernameMissing() throws Exception {
        when(serviceJwtValidator.isValid("valid-token")).thenReturn(true);
        var request = new TrainerWorkloadRequest(" ", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, ActionType.ADD);

        mockMvc.perform(post("/api/trainer-workloads")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_shouldReturn200_whenAuthorized() throws Exception {
        when(serviceJwtValidator.isValid("valid-token")).thenReturn(true);
        when(workloadService.getSummary("Jane.Doe")).thenReturn(
                new TrainerWorkloadSummaryResponse("Jane.Doe", "Jane", "Doe", true, List.of()));

        mockMvc.perform(get("/api/trainer-workloads/Jane.Doe").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("Jane.Doe"));
    }

    @Test
    void getSummary_shouldReturn404_whenNoData() throws Exception {
        when(serviceJwtValidator.isValid("valid-token")).thenReturn(true);
        when(workloadService.getSummary("Unknown")).thenThrow(new EntityNotFoundException("No workload data found for trainer: Unknown"));

        mockMvc.perform(get("/api/trainer-workloads/Unknown").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }
}
