package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.request.AddTrainingRequest;
import com.gym.exception.AuthenticationException;
import com.gym.exception.EntityNotFoundException;
import com.gym.service.impl.TrainerService;
import com.gym.service.impl.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainingService trainingService;
    @MockBean
    private TrainerService trainerService;

    @Test
    void addTraining_shouldReturn200_whenValidRequest() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainings")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addTraining_shouldReturn400_whenTrainingNameMissing() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", " ", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainings")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_shouldReturn400_whenPasswordHeaderMissing() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTraining_shouldReturn401_whenTrainerAuthFails() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);
        when(trainerService.authenticate("Jane.Doe", "wrong"))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(post("/api/trainings")
                        .header("Password", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addTraining_shouldReturn404_whenTraineeNotFound() throws Exception {
        var request = new AddTrainingRequest("Unknown", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);
        when(trainingService.addTraining("Unknown", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60))
                .thenThrow(new EntityNotFoundException("Trainee not found: Unknown"));

        mockMvc.perform(post("/api/trainings")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
