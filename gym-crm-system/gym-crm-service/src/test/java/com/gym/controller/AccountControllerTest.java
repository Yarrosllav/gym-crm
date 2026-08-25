package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.request.ChangePasswordRequest;
import com.gym.exception.AuthenticationException;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import com.gym.service.impl.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(MethodSecurityTestConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private AccountService accountService;

    @Test
    void login_shouldReturn200_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/login").param("username", "John.Smith").header("Password", "pwd"))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(accountService).login("John.Smith", "wrong");

        mockMvc.perform(get("/api/auth/login").param("username", "John.Smith").header("Password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer some-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_shouldUsePrincipalUsername() throws Exception {
        var request = new ChangePasswordRequest("old", "newPwd1234");

        mockMvc.perform(put("/api/auth/password")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(accountService).changePassword("John.Smith", "old", "newPwd1234");
    }

    @Test
    void changePassword_shouldReturn400_whenNewPasswordBlank() throws Exception {
        var json = """
                {"oldPassword":"old","newPassword":" "}
                """;

        mockMvc.perform(put("/api/auth/password")
                        .with(user("John.Smith").roles("TRAINEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
