package com.gym.report.controller;

import com.gym.report.dto.TrainerWorkloadSummaryResponse;
import com.gym.report.exception.EntityNotFoundException;
import com.gym.report.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkloadController.class)
class WorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkloadService workloadService;

    @Test
    void getSummary_shouldReturn200() throws Exception {
        when(workloadService.getSummary("Jane.Doe")).thenReturn(
                new TrainerWorkloadSummaryResponse("Jane.Doe", "Jane", "Doe", true, List.of()));

        mockMvc.perform(get("/api/trainer-workloads/Jane.Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("Jane.Doe"));
    }

    @Test
    void getSummary_shouldReturn404_whenNoData() throws Exception {
        when(workloadService.getSummary("Unknown"))
                .thenThrow(new EntityNotFoundException("No workload data found for trainer: Unknown"));

        mockMvc.perform(get("/api/trainer-workloads/Unknown"))
                .andExpect(status().isNotFound());
    }
}
