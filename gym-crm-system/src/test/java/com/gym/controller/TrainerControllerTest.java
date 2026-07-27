package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.converter.TrainerToProfileConverter;
import com.gym.converter.TrainerToRegistrationResponseConverter;
import com.gym.converter.TrainingToTrainerTrainingConverter;
import com.gym.dto.request.ActivateRequest;
import com.gym.dto.request.TrainerRegistrationRequest;
import com.gym.dto.request.TrainerUpdateRequest;
import com.gym.dto.response.TrainerProfileResponse;
import com.gym.exception.AuthenticationException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.service.impl.TrainerService;
import com.gym.service.impl.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainerService trainerService;
    @MockBean
    private TrainingService trainingService;
    @MockBean
    private TrainerToRegistrationResponseConverter registrationConverter;
    @MockBean
    private TrainerToProfileConverter profileConverter;
    @MockBean
    private TrainingToTrainerTrainingConverter trainingConverter;

    private Trainer buildTrainer(String username) {
        var user = new User();
        user.setUsername(username);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setActive(true);
        var trainer = new Trainer();
        trainer.setUser(user);
        var specialization = new TrainingType(1L, "Cardio");
        trainer.setSpecialization(specialization);
        return trainer;
    }

    @Test
    void register_shouldReturn200_whenValidRequest() throws Exception {
        var request = new TrainerRegistrationRequest("Jane", "Doe", 1L);
        var trainer = buildTrainer("Jane.Doe");
        when(trainerService.createProfile("Jane", "Doe", 1L)).thenReturn(trainer);
        when(registrationConverter.convert(trainer))
                .thenReturn(new com.gym.dto.response.RegistrationResponse("Jane.Doe", "pwd1234567"));

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane.Doe"));
    }

    @Test
    void register_shouldReturn400_whenSpecializationMissing() throws Exception {
        var request = new TrainerRegistrationRequest("Jane", "Doe", null);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfile_shouldReturn200_whenAuthenticated() throws Exception {
        var trainer = buildTrainer("Jane.Doe");
        when(trainerService.getProfile("Jane.Doe", "pwd")).thenReturn(trainer);
        when(profileConverter.convert(trainer)).thenReturn(
                new com.gym.dto.response.TrainerProfileResponse(
                        "Jane.Doe", "Jane", "Doe", "Cardio", true, List.of()));

        mockMvc.perform(get("/api/trainers/Jane.Doe").header("Password", "pwd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialization").value("Cardio"));
    }

    @Test
    void getProfile_shouldReturn401_whenAuthenticationFails() throws Exception {
        when(trainerService.getProfile("Jane.Doe", "wrong"))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(get("/api/trainers/Jane.Doe").header("Password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_shouldReturn200_andNotTouchSpecialization() throws Exception {
        var request = new TrainerUpdateRequest("Janet", "Doey", true);
        var trainer = buildTrainer("Jane.Doe");
        when(trainerService.updateProfileAndStatus(
                eq("Jane.Doe"),
                eq("pwd"),
                eq("Janet"),
                eq("Doey"),
                eq(true))).thenReturn(trainer);

        when(profileConverter.convert(trainer)).thenReturn(
                new TrainerProfileResponse(
                        "Jane.Doe", "Janet", "Doey", "Cardio",
                        true, List.of()));

        mockMvc.perform(put("/api/trainers/Jane.Doe")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void setActive_shouldReturn400_whenAlreadyInThatState() throws Exception {
        var request = new ActivateRequest(true);
        doThrow(new ValidationException("Profile is already active"))
                .when(trainerService).setActive("Jane.Doe", "pwd", true);

        mockMvc.perform(patch("/api/trainers/Jane.Doe/status")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainings_shouldReturn200() throws Exception {
        when(trainingService.getTrainerTrainings("Jane.Doe", null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/Jane.Doe/trainings").header("Password", "pwd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
