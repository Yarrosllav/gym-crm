package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.request.ChangeLoginRequest;
import com.gym.exception.AuthenticationException;
import com.gym.service.impl.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void login_shouldReturn200_whenCredentialsMatch() throws Exception {
        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Smith")
                        .header("Password", "pwd"))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturn401_whenCredentialsDoNotMatch() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(accountService).login("John.Smith", "wrong");

        mockMvc.perform(get("/api/auth/login")
                        .param("username", "John.Smith")
                        .header("Password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn400_whenUsernameParamMissing() throws Exception {
        mockMvc.perform(get("/api/auth/login").header("Password", "pwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeLogin_shouldReturn200_whenValidRequest() throws Exception {
        var request = new ChangeLoginRequest("John.Smith", "old", "newPwd1234");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changeLogin_shouldReturn400_whenNewPasswordBlank() throws Exception {
        var request = new ChangeLoginRequest("John.Smith", "old", " ");

        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
