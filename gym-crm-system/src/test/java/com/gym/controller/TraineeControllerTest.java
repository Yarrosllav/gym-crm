package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.converter.TraineeToProfileConverter;
import com.gym.converter.TraineeToRegistrationResponseConverter;
import com.gym.converter.TrainerToSummaryConverter;
import com.gym.converter.TrainingToTraineeTrainingConverter;
import com.gym.dto.request.ActivateRequest;
import com.gym.dto.request.TraineeRegistrationRequest;
import com.gym.dto.request.TraineeUpdateRequest;
import com.gym.dto.request.TrainersListUpdateRequest;
import com.gym.dto.response.TraineeProfileResponse;
import com.gym.dto.response.TrainerSummaryResponse;
import com.gym.exception.AuthenticationException;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.service.impl.TraineeService;
import com.gym.service.impl.TrainerService;
import com.gym.service.impl.TrainingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
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
    private TraineeToRegistrationResponseConverter registrationConverter;
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
        var trainee = new Trainee();
        trainee.setUser(user);
        return trainee;
    }

    @Test
    void register_shouldReturn200_whenValidRequest() throws Exception {
        var request = new TraineeRegistrationRequest("John", "Smith", LocalDate.of(2000, 1, 1), "Main St");
        var trainee = buildTrainee("John.Smith");
        when(traineeService.createProfile("John", "Smith", request.dateOfBirth(), request.address()))
                .thenReturn(trainee);
        when(registrationConverter.convert(trainee))
                .thenReturn(new com.gym.dto.response.RegistrationResponse("John.Smith", "pwd1234567"));

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"));
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
        when(traineeService.getProfile("John.Smith", "pwd")).thenReturn(trainee);
        when(profileConverter.convert(trainee)).thenReturn(
                new com.gym.dto.response.TraineeProfileResponse(
                        "John.Smith", "John", "Smith", null, null, true, java.util.List.of()));

        mockMvc.perform(get("/api/trainees/John.Smith").header("Password", "pwd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"));
    }

    @Test
    void getProfile_shouldReturn401_whenAuthenticationFails() throws Exception {
        when(traineeService.getProfile("John.Smith", "wrong"))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(get("/api/trainees/John.Smith").header("Password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_shouldReturn400_whenPasswordHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/trainees/John.Smith"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfile_shouldReturn200_whenValidRequest() throws Exception {
        var request = new TraineeUpdateRequest(
                "Johnny", "Smithy", LocalDate.of(1995, 5, 5), "New Address", true);
        var trainee = buildTrainee("John.Smith");

        when(traineeService.updateProfileAndStatus(
                eq("John.Smith"), eq("pwd"),
                eq("Johnny"), eq("Smithy"),
                eq(LocalDate.of(1995, 5, 5)), eq("New Address"), eq(true)))
                .thenReturn(trainee);
        when(profileConverter.convert(trainee)).thenReturn(
                new TraineeProfileResponse(
                        "John.Smith", "Johnny", "Smithy",
                        LocalDate.of(1995, 5, 5), "New Address", true, List.of()));

        mockMvc.perform(put("/api/trainees/John.Smith")
                        .header("Password", "pwd")
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
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/trainees/John.Smith").header("Password", "pwd"))
                .andExpect(status().isOk());
    }

    @Test
    void setActive_shouldReturn200_whenValidRequest() throws Exception {
        var request = new ActivateRequest(false);

        mockMvc.perform(patch("/api/trainees/John.Smith/status")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void setActive_shouldReturn400_whenAlreadyInThatState() throws Exception {
        var request = new ActivateRequest(true);
        org.mockito.Mockito.doThrow(new ValidationException("Profile is already active"))
                .when(traineeService).setActive("John.Smith", "pwd", true);

        mockMvc.perform(patch("/api/trainees/John.Smith/status")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTrainersList_shouldReturn200_andReturnMappedTrainers() throws Exception {
        var request = new TrainersListUpdateRequest(java.util.List.of("Jane.Doe"));
        var trainer = new Trainer();
        when(traineeService.updateTrainersList("John.Smith", "pwd", request.trainerUsernames()))
                .thenReturn(Set.of(trainer));
        when(trainerSummaryConverter.convert(trainer)).thenReturn(
                new TrainerSummaryResponse("Jane.Doe", "Jane", "Doe", "Cardio"));

        mockMvc.perform(put("/api/trainees/John.Smith/trainers")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Jane.Doe"));
    }

    @Test
    void updateTrainersList_shouldReturn404_whenTrainerNotFound() throws Exception {
        var request = new TrainersListUpdateRequest(java.util.List.of("Unknown"));
        when(traineeService.updateTrainersList(eq("John.Smith"), eq("pwd"), anyList()))
                .thenThrow(new EntityNotFoundException("Trainer not found: Unknown"));

        mockMvc.perform(put("/api/trainees/John.Smith/trainers")
                        .header("Password", "pwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTrainings_shouldReturn200_withOptionalFiltersAbsent() throws Exception {
        when(trainingService.getTraineeTrainings("John.Smith", null, null, null, null))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/trainees/John.Smith/trainings").header("Password", "pwd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
