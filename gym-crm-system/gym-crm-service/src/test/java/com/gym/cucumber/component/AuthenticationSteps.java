package com.gym.cucumber.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationSteps {

    private final CucumberSpringConfiguration context;
    private final TestContext testContext;
    private final TestRestTemplate rest = new TestRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationSteps(CucumberSpringConfiguration context, TestContext testContext) {
        this.context = context;
        this.testContext = testContext;
    }

    @Given("a trainee {string} is registered")
    public void registerTrainee(String fullName) {
        var names = fullName.split("\\.");
        var body = Map.of(
                "firstName", names[0], "lastName", names[1],
                "dateOfBirth", "2000-01-01", "address", "Main St");

        var response = rest.postForEntity(context.baseUrl() + "/api/trainees", body, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("Registration should succeed: %s", response.getBody())
                .isTrue();

        storeCredentials(fullName, response.getBody());
    }

    @Given("a trainer {string} is registered")
    public void registerTrainer(String fullName) {
        var names = fullName.split("\\.");
        var body = Map.of("firstName", names[0], "lastName", names[1], "specializationId", 1);

        var response = rest.postForEntity(context.baseUrl() + "/api/trainers", body, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("Registration should succeed: %s", response.getBody())
                .isTrue();

        storeCredentials(fullName, response.getBody());
    }

    @Given("an admin user {string} is authenticated")
    public void adminIsAuthenticated(String username) {
        testContext.putPassword(username, AdminTestSeeder.ADMIN_PASSWORD);
        login(username, AdminTestSeeder.ADMIN_PASSWORD);
        testContext.putToken(username, extractToken(testContext.getLastResponse().getBody()));
    }

    @When("{string} logs in with password {string}")
    public void login(String username, String password) {
        var headers = new HttpHeaders();
        headers.set("Password", password);
        testContext.setLastResponse(rest.exchange(
                context.baseUrl() + "/api/auth/login?username=" + username,
                HttpMethod.GET, new HttpEntity<>(headers), String.class));
    }

    @Given("{string} fails to log in with wrong password {int} times")
    public void failLoginNTimes(String username, int times) {
        for (int i = 0; i < times; i++) {
            login(username, "WrongPassword");
        }
    }

    @Given("{string} is authenticated")
    public void isAuthenticated(String username) {
        var correctPassword = testContext.getPassword(username);
        assertThat(correctPassword)
                .as("User %s must be registered before authenticating", username)
                .isNotNull();

        login(username, correctPassword);
        assertThat(testContext.getLastResponse().getStatusCode().is2xxSuccessful())
                .as("Login should succeed: %s", testContext.getLastResponse().getBody())
                .isTrue();

        testContext.putToken(username, extractToken(testContext.getLastResponse().getBody()));
    }

    @When("{string} requests the profile of trainee {string}")
    public void requestTraineeProfile(String requester, String targetUsername) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(tokenOrFail(requester));
        testContext.setLastResponse(rest.exchange(
                context.baseUrl() + "/api/trainees/" + targetUsername,
                HttpMethod.GET, new HttpEntity<>(headers), String.class));
    }

    @When("an unauthenticated request is made to get profile of trainee {string}")
    public void unauthenticatedRequest(String username) {
        testContext.setLastResponse(rest.getForEntity(
                context.baseUrl() + "/api/trainees/" + username, String.class));
    }

    @Then("the response status should be {int}")
    public void checkStatus(int expectedStatus) {
        assertThat(testContext.getLastResponse().getStatusCode().value())
                .as("Response body: %s", testContext.getLastResponse().getBody())
                .isEqualTo(expectedStatus);
    }

    @Then("the response should contain a valid JWT token")
    public void checkToken() {
        var token = extractToken(testContext.getLastResponse().getBody());
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Then("the response message should contain {string}")
    public void checkMessage(String expectedFragment) {
        assertThat(testContext.getLastResponse().getBody()).containsIgnoringCase(expectedFragment);
    }

    private void storeCredentials(String humanKey, String responseBody) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
            var actualUsername = json.get("username").asText();
            var generatedPassword = json.get("password").asText();

            testContext.putPassword(humanKey, generatedPassword);
            testContext.putPassword(actualUsername, generatedPassword);

            if (json.has("token")) {
                var token = json.get("token").asText();
                testContext.putToken(humanKey, token);
                testContext.putToken(actualUsername, token);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse registration response: " + responseBody, e);
        }
    }

    private String extractToken(String responseBody) {
        try {
            JsonNode json = objectMapper.readTree(responseBody);
            return json.has("token") ? json.get("token").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    String tokenOrFail(String username) {
        var token = testContext.getToken(username);
        assertThat(token).as("No token found for %s — is it authenticated?", username).isNotNull();
        return token;
    }
}
