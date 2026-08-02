package com.gym.controller;

import com.gym.dto.request.ChangeLoginRequest;
import com.gym.service.impl.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/login")
    @Operation(summary = "Check username/password match, regardless of role")
    public ResponseEntity<Void> login(
            @RequestParam String username,
            @RequestHeader("Password") String password) {
        accountService.login(username, password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password (regardless of role)")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangeLoginRequest request) {
        accountService.changePassword(request.username(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
