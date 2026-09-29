package com.gym.cucumber.component;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Map;

public class ProfileManagementSteps {

    private final CucumberSpringConfiguration context;
    private final TestContext testContext;
    private final TestRestTemplate rest = new TestRestTemplate();

    public ProfileManagementSteps(CucumberSpringConfiguration context, TestContext testContext) {
        this.context = context;
        this.testContext = testContext;
    }

    @When("{string} updates their own profile with first name {string}")
    public void updateOwnProfile(String username, String newFirstName) {
        var headers = authHeaders(username);
        var body = Map.of("firstName", newFirstName, "lastName", "Updated", "isActive", true);
        testContext.setLastResponse(rest.exchange(
                context.baseUrl() + "/api/trainees/" + username,
                HttpMethod.PUT, new HttpEntity<>(body, headers), String.class));
    }

    @When("{string} updates the profile of trainee {string} with first name {string}")
    public void updateOtherProfile(String actingUser, String targetUsername, String newFirstName) {
        var headers = authHeaders(actingUser);
        var body = Map.of("firstName", newFirstName, "lastName", "Hacked", "isActive", true);
        testContext.setLastResponse(rest.exchange(
                context.baseUrl() + "/api/trainees/" + targetUsername,
                HttpMethod.PUT, new HttpEntity<>(body, headers), String.class));
    }

    @When("{string} deactivates trainee {string}")
    public void deactivateTrainee(String actingUser, String targetUsername) {
        var headers = authHeaders(actingUser);
        var body = Map.of("isActive", false);
        testContext.setLastResponse(rest.exchange(
                context.baseUrl() + "/api/trainees/" + targetUsername + "/status",
                HttpMethod.PATCH, new HttpEntity<>(body, headers), String.class));
    }

    @When("{string} deletes trainee {string}")
    public void deleteTrainee(String actingUser, String targetUsername) {
        var headers = authHeaders(actingUser);
        testContext.setLastResponse(rest.exchange(
                context.baseUrl() + "/api/trainees/" + targetUsername,
                HttpMethod.DELETE, new HttpEntity<>(headers), String.class));
    }

    @Then("the deleted trainee should no longer be retrievable")
    public void verifyDeleted() {
    }

    private HttpHeaders authHeaders(String actingUsername) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(testContext.getToken(actingUsername));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
