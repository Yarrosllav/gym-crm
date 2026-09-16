package com.gym.report.cucumber;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestContext {
    public ResponseEntity<String> lastResponse;
    public String currentTrainerUsername;
}
