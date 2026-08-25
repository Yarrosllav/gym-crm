package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.converter.TrainerToProfileConverter;
import com.gym.converter.TrainingToTrainerTrainingConverter;
import com.gym.dto.request.ActivateRequest;
import com.gym.dto.request.TrainerRegistrationRequest;
import com.gym.dto.request.TrainerUpdateRequest;
import com.gym.dto.response.TrainerProfileResponse;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Role;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
@Import(MethodSecurityTestConfig.class)
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
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
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
        user.setRole(Role.ROLE_TRAINER);
        var trainer = new Trainer();
        trainer.setUser(user);
        var specialization = new TrainingType(1L, "Cardio");
        trainer.setSpecialization(specialization);
        return trainer;
    }

    @Test
    void register_shouldReturn200_withoutAuthentication_whenValidRequest() throws Exception {
        var request = new TrainerRegistrationRequest("Jane", "Doe", 1L);
        var trainer = buildTrainer("Jane.Doe");
        when(trainerService.createProfile("Jane", "Doe", 1L))
                .thenReturn(new TrainerService.TrainerRegistrationResult(trainer, "rawPass123"));
        when(jwtService.generateToken("Jane.Doe", "ROLE_TRAINER")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane.Doe"))
                .andExpect(jsonPath("$.password").value("rawPass123"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
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
    void register_shouldReturn400_whenAlreadyRegisteredAsTrainee() throws Exception {
        var request = new TrainerRegistrationRequest("Jane", "Doe", 1L);
        when(trainerService.createProfile("Jane", "Doe", 1L))
                .thenThrow(new ValidationException("This person is already registered as a Trainee"));

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfile_shouldReturn200_whenAuthenticated() throws Exception {
        var trainer = buildTrainer("Jane.Doe");
        when(trainerService.getProfile("Jane.Doe")).thenReturn(trainer);
        when(profileConverter.convert(trainer)).thenReturn(
                new TrainerProfileResponse("Jane.Doe", "Jane", "Doe", "Cardio", true, List.of()));

        mockMvc.perform(get("/api/trainers/Jane.Doe").with(user("Jane.Doe").roles("TRAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialization").value("Cardio"));
    }

    @Test
    void getProfile_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/trainers/Jane.Doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
        mockMvc.perform(get("/api/trainers/Jane.Doe").with(user("Other.User").roles("TRAINER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_shouldReturn403_whenTraineeRequestsTrainerEndpointWithSameUsername() throws Exception {
        mockMvc.perform(get("/api/trainers/Jane.Doe").with(user("Jane.Doe").roles("TRAINEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_shouldReturn200_whenAdminRequestsAnyProfile() throws Exception {
        var trainer = buildTrainer("Jane.Doe");
        when(trainerService.getProfile("Jane.Doe")).thenReturn(trainer);
        when(profileConverter.convert(trainer)).thenReturn(
                new TrainerProfileResponse("Jane.Doe", "Jane", "Doe", "Cardio", true, List.of()));

        mockMvc.perform(get("/api/trainers/Jane.Doe").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_shouldReturn404_whenProfileNotFound() throws Exception {
        when(trainerService.getProfile("Unknown")).thenThrow(new EntityNotFoundException("Profile not found: Unknown"));

        mockMvc.perform(get("/api/trainers/Unknown").with(user("Unknown").roles("TRAINER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_shouldReturn200_andNotTouchSpecialization() throws Exception {
        var request = new TrainerUpdateRequest("Janet", "Doey", true);
        var trainer = buildTrainer("Jane.Doe");
        when(trainerService.updateProfileAndStatus(eq("Jane.Doe"), eq("Janet"), eq("Doey"), eq(true)))
                .thenReturn(trainer);
        when(profileConverter.convert(trainer)).thenReturn(
                new TrainerProfileResponse("Jane.Doe", "Janet", "Doey", "Cardio", true, List.of()));

        mockMvc.perform(put("/api/trainers/Jane.Doe")
                        .with(user("Jane.Doe").roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
        var request = new TrainerUpdateRequest("Janet", "Doey", true);

        mockMvc.perform(put("/api/trainers/Jane.Doe")
                        .with(user("Other.User").roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void setActive_shouldReturn400_whenAlreadyInThatState() throws Exception {
        var request = new ActivateRequest(true);
        doThrow(new ValidationException("Profile is already active"))
                .when(trainerService).setActive("Jane.Doe", true);

        mockMvc.perform(patch("/api/trainers/Jane.Doe/status")
                        .with(user("Jane.Doe").roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setActive_shouldReturn200_whenAdmin() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/trainers/Jane.Doe/status")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void setActive_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/trainers/Jane.Doe/status")
                        .with(user("Other.User").roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTrainings_shouldReturn200_whenOwner() throws Exception {
        when(trainingService.getTrainerTrainings("Jane.Doe", null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/Jane.Doe/trainings").with(user("Jane.Doe").roles("TRAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getTrainings_shouldReturn403_whenNotOwner() throws Exception {
        mockMvc.perform(get("/api/trainers/Jane.Doe/trainings").with(user("Other.User").roles("TRAINER")))
                .andExpect(status().isForbidden());
    }
}
