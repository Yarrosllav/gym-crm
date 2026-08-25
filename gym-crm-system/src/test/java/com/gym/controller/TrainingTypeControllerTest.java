package com.gym.controller;

import com.gym.converter.TrainingTypeToResponseConverter;
import com.gym.dto.response.TrainingTypeResponse;
import com.gym.model.TrainingType;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import com.gym.service.impl.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingTypeController.class)
@Import(MethodSecurityTestConfig.class)
class TrainingTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingTypeService trainingTypeService;
    @MockBean
    private TrainingTypeToResponseConverter converter;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void getAll_shouldReturnListOfTrainingTypes() throws Exception {
        var type = new TrainingType();
        when(trainingTypeService.getAll()).thenReturn(List.of(type));
        when(converter.convert(type)).thenReturn(new TrainingTypeResponse(1L, "Cardio"));

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cardio"));
    }
}
