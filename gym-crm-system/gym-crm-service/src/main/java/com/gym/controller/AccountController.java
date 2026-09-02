package com.gym.controller;

import com.gym.dto.request.ChangePasswordRequest;
import com.gym.dto.response.LoginResponse;
import com.gym.service.impl.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/login")
    @Operation(summary = "Login and receive a JWT bearer token")
    public ResponseEntity<LoginResponse> login(
            @RequestParam String username,
            @RequestHeader("Password") String password) {
        var token = accountService.login(username, password);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate the current bearer token")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        accountService.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password")
    public ResponseEntity<Void> changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(principal.getName(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
