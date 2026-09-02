package com.gym.report.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404() {
        var response = handler.handleNotFound(new EntityNotFoundException("No workload data found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No workload data found", response.getBody().message());
    }

    @Test
    void handleValidation_shouldReturn400_withFieldErrorMessage() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("request", "trainerUsername", "Trainer username is required");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        var ex = new MethodArgumentNotValidException(
                mock(org.springframework.core.MethodParameter.class), bindingResult);

        var response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("trainerUsername: Trainer username is required", response.getBody().message());
    }

    @Test
    void handleUnexpected_shouldReturn500_andNotLeakStackTrace() {
        var response = handler.handleUnexpected(new NullPointerException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred", response.getBody().message());
    }
}
