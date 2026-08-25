package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.request.AddTrainingRequest;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import com.gym.service.impl.TrainerService;
import com.gym.service.impl.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@Import(MethodSecurityTestConfig.class)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private TrainingService trainingService;
    @MockBean
    private TrainerService trainerService;

    @Test
    void addTraining_shouldReturn200_whenOwnTrainerUsernameMatches() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainings")
                        .with(user("Jane.Doe").roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addTraining_shouldReturn403_whenTrainerUsernameDoesNotMatchToken() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainings")
                        .with(user("Someone.Else").roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addTraining_shouldReturn200_whenAdmin() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainings")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addTraining_shouldReturn403_whenTraineeAttempts() throws Exception {
        var request = new AddTrainingRequest("John.Smith", "Jane.Doe", "Morning Run", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainings")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
