package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.converter.TraineeToProfileConverter;
import com.gym.converter.TrainerToSummaryConverter;
import com.gym.converter.TrainingToTraineeTrainingConverter;
import com.gym.dto.request.ActivateRequest;
import com.gym.dto.request.TraineeRegistrationRequest;
import com.gym.dto.request.TraineeUpdateRequest;
import com.gym.dto.request.TrainersListUpdateRequest;
import com.gym.dto.response.TraineeProfileResponse;
import com.gym.dto.response.TrainerSummaryResponse;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Role;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import com.gym.service.impl.TraineeService;
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
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@Import(MethodSecurityTestConfig.class)
class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TraineeService traineeService;
    @MockBean
    private TrainerService trainerService;
    @MockBean
    private TrainingService trainingService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private TraineeToProfileConverter profileConverter;
    @MockBean
    private TrainerToSummaryConverter trainerSummaryConverter;
    @MockBean
    private TrainingToTraineeTrainingConverter trainingConverter;

    private Trainee buildTrainee(String username) {
        var user = new User();
        user.setUsername(username);
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setActive(true);
        user.setRole(Role.ROLE_TRAINEE);
        var trainee = new Trainee();
        trainee.setUser(user);
        return trainee;
    }

    @Test
    void register_shouldReturn200_withoutAuthentication_whenValidRequest() throws Exception {
        var request = new TraineeRegistrationRequest("John", "Smith", LocalDate.of(2000, 1, 1), "Main St");
        var trainee = buildTrainee("John.Smith");
        when(traineeService.createProfile("John", "Smith", request.dateOfBirth(), request.address()))
                .thenReturn(new TraineeService.TraineeRegistrationResult(trainee, "rawPass123"));
        when(jwtService.generateToken("John.Smith", "ROLE_TRAINEE")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("rawPass123"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_shouldReturn400_whenFirstNameMissing() throws Exception {
        var request = new TraineeRegistrationRequest(" ", "Smith", null, null);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfile_shouldReturn200_whenAuthenticated() throws Exception {
        var trainee = buildTrainee("John.Smith");
        when(traineeService.getProfile("John.Smith")).thenReturn(trainee);
        when(profileConverter.convert(trainee)).thenReturn(
                new TraineeProfileResponse(
                        "John.Smith", "John", "Smith", null, null, true, List.of()));

        mockMvc.perform(get("/api/trainees/John.Smith").with(user("John.Smith").roles("TRAINEE")))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Smith"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_shouldReturn403_whenTraineeRequestsDifferentProfile() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Smith").with(user("Other.User").roles("TRAINEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_shouldReturn403_whenTrainerRequestsTraineeEndpointWithSameUsername() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Smith").with(user("John.Smith").roles("TRAINER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_shouldReturn200_whenAdminRequestsAnyProfile() throws Exception {
        var trainee = buildTrainee("John.Smith");
        when(traineeService.getProfile("John.Smith")).thenReturn(trainee);
        when(profileConverter.convert(trainee)).thenReturn(
                new TraineeProfileResponse("John.Smith", "John", "Smith", null, null, true, List.of()));

        mockMvc.perform(get("/api/trainees/John.Smith").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_shouldReturn404_whenProfileNotFound() throws Exception {
        when(traineeService.getProfile("Unknown")).thenThrow(new EntityNotFoundException("Profile not found: Unknown"));

        mockMvc.perform(get("/api/trainees/Unknown").with(user("Unknown").roles("TRAINEE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_shouldReturn200_whenValidRequest() throws Exception {
        var request = new TraineeUpdateRequest("Johnny", "Smithy", LocalDate.of(1995, 5, 5), "New Address", true);
        var trainee = buildTrainee("John.Smith");

        when(traineeService.updateProfileAndStatus(
                eq("John.Smith"), eq("Johnny"), eq("Smithy"),
                eq(LocalDate.of(1995, 5, 5)), eq("New Address"), eq(true)))
                .thenReturn(trainee);
        when(profileConverter.convert(trainee)).thenReturn(
                new TraineeProfileResponse("John.Smith", "Johnny", "Smithy",
                        LocalDate.of(1995, 5, 5), "New Address", true, List.of()));

        mockMvc.perform(put("/api/trainees/John.Smith")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"));
    }

    @Test
    void updateProfile_shouldReturn400_whenIsActiveMissing() throws Exception {
        var json = """
                {"firstName":"Johnny","lastName":"Smithy","dateOfBirth":null,"address":null}
                """;

        mockMvc.perform(put("/api/trainees/John.Smith")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfile_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
        var request = new TraineeUpdateRequest("Johnny", "Smithy", null, null, true);

        mockMvc.perform(put("/api/trainees/John.Smith")
                        .with(user("Other.User").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_shouldReturn200_whenOwnerDeletesOwnProfile() throws Exception {
        mockMvc.perform(delete("/api/trainees/John.Smith").with(user("John.Smith").roles("TRAINEE")))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/trainees/John.Smith").with(user("Other.User").roles("TRAINEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_shouldReturn200_whenAdminDeletesAnyProfile() throws Exception {
        mockMvc.perform(delete("/api/trainees/John.Smith").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void setActive_shouldReturn200_whenAdminActivatesAnyAccount() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/trainees/John.Smith/status")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void setActive_shouldReturn400_whenAlreadyInThatState() throws Exception {
        var request = new ActivateRequest(true);
        org.mockito.Mockito.doThrow(new ValidationException("Profile is already active"))
                .when(traineeService).setActive("John.Smith", true);

        mockMvc.perform(patch("/api/trainees/John.Smith/status")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setActive_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/trainees/John.Smith/status")
                        .with(user("Other.User").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTrainersList_shouldReturn200_andReturnMappedTrainers() throws Exception {
        var request = new TrainersListUpdateRequest(List.of("Jane.Doe"));
        var trainer = new Trainer();
        when(traineeService.updateTrainersList("John.Smith", request.trainerUsernames()))
                .thenReturn(Set.of(trainer));
        when(trainerSummaryConverter.convert(trainer)).thenReturn(
                new TrainerSummaryResponse("Jane.Doe", "Jane", "Doe", "Cardio"));

        mockMvc.perform(put("/api/trainees/John.Smith/trainers")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Jane.Doe"));
    }

    @Test
    void updateTrainersList_shouldReturn404_whenTrainerNotFound() throws Exception {
        var request = new TrainersListUpdateRequest(List.of("Unknown"));
        when(traineeService.updateTrainersList(eq("John.Smith"), anyList()))
                .thenThrow(new EntityNotFoundException("Trainer not found: Unknown"));

        mockMvc.perform(put("/api/trainees/John.Smith/trainers")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUnassignedTrainers_shouldReturn200_whenOwner() throws Exception {
        when(trainerService.getTrainersNotAssignedToTrainee("John.Smith")).thenReturn(List.of());

        mockMvc.perform(get("/api/trainees/John.Smith/unassigned-trainers")
                        .with(user("John.Smith").roles("TRAINEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUnassignedTrainers_shouldReturn403_whenNotOwner() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Smith/unassigned-trainers")
                        .with(user("Other.User").roles("TRAINEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTrainings_shouldReturn200_withOptionalFiltersAbsent() throws Exception {
        when(trainingService.getTraineeTrainings("John.Smith", null, null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainees/John.Smith/trainings").with(user("John.Smith").roles("TRAINEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getTrainings_shouldReturn403_whenNotOwner() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Smith/trainings").with(user("Other.User").roles("TRAINEE")))
                .andExpect(status().isForbidden());
    }
}
