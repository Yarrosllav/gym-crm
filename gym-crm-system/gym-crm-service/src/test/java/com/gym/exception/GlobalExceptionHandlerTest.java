package com.gym.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @SuppressWarnings("unused")
    private void dummyMethod(String header) {
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("dummyMethod", String.class);
        return new MethodParameter(method, 0);
    }

    @Test
    void handleValidation_shouldReturnBadRequest() {
        var response = handler.handleValidation(new ValidationException("First name is required"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("First name is required", response.getBody().message());
    }

    @Test
    void handleBeanValidation_shouldJoinAllFieldErrors() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "firstName", "must not be blank"));
        bindingResult.addError(new FieldError("request", "lastName", "must not be blank"));
        var ex = new MethodArgumentNotValidException(methodParameter(), bindingResult);

        var response = handler.handleBeanValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("firstName: must not be blank; lastName: must not be blank", response.getBody().message());
    }

    @Test
    void handleBeanValidation_shouldReturnDefaultMessage_whenNoFieldErrors() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        var ex = new MethodArgumentNotValidException(methodParameter(), bindingResult);

        var response = handler.handleBeanValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().message());
    }

    @Test
    void handleAuth_shouldReturnUnauthorized() {
        var response = handler.handleAuth(new AuthenticationException("Invalid password for user: John.Smith"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid password for user: John.Smith", response.getBody().message());
    }

    @Test
    void handleNotFound_shouldReturnNotFound() {
        var response = handler.handleNotFound(new EntityNotFoundException("Trainer not found: Jane.Doe"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Trainer not found: Jane.Doe", response.getBody().message());
    }

    @Test
    void handleMissingParam_shouldReturnBadRequest_forMissingHeader() throws NoSuchMethodException {
        var ex = new MissingRequestHeaderException("Password", methodParameter());

        var response = handler.handleMissingParam(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMissingParam_shouldReturnBadRequest_forMissingRequestParam() {
        var ex = new MissingServletRequestParameterException("periodFrom", "LocalDate");

        var response = handler.handleMissingParam(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleUnexpected_shouldReturnInternalServerError() {
        var response = handler.handleUnexpected(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred", response.getBody().message());
    }
}
