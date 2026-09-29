package com.gym.cucumber.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope(scopeName = "cucumber-glue", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TestContext {

    private final Map<String, String> passwordsByUsername = new HashMap<>();
    private final Map<String, String> tokensByUsername = new HashMap<>();
    @Getter
    @Setter
    private ResponseEntity<String> lastResponse;
    @Getter
    @Setter
    private String currentTrainerUsername;

    public void putPassword(String username, String password) {
        passwordsByUsername.put(username, password);
    }

    public String getPassword(String username) {
        return passwordsByUsername.get(username);
    }

    public void putToken(String username, String token) {
        tokensByUsername.put(username, token);
    }

    public String getToken(String username) {
        return tokensByUsername.get(username);
    }
}
