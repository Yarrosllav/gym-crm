package com.gym.cucumber.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadSyncSteps {

    private static final String GYM_CRM_BASE_URL =
            System.getProperty("gym.crm.base-url", "http://localhost:8080");
    private static final String REPORT_SERVICE_BASE_URL =
            System.getProperty("report.service.base-url", "http://localhost:8081");

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> usernamesByFixtureKey = new HashMap<>();
    private String jwtToken;
    private HttpResponse<String> lastResponse;

    @Given("a trainer {string} is registered")
    public void registerTrainer(String fixtureKey) throws Exception {
        var names = uniqueNameParts(fixtureKey);
        var body = objectMapper.writeValueAsString(Map.of(
                "firstName", names[0], "lastName", names[1], "specializationId", 1));

        var response = post(GYM_CRM_BASE_URL + "/api/trainers", body, null);
        var json = objectMapper.readTree(response.body());
        var actualUsername = json.get("username").asText();

        usernamesByFixtureKey.put(fixtureKey, actualUsername);
        jwtToken = json.get("token").asText();
    }

    @Given("a trainee {string} is registered")
    public void registerTrainee(String fixtureKey) throws Exception {
        var names = uniqueNameParts(fixtureKey);
        var body = objectMapper.writeValueAsString(Map.of(
                "firstName", names[0], "lastName", names[1],
                "dateOfBirth", "2000-01-01", "address", "Main St"));

        var response = post(GYM_CRM_BASE_URL + "/api/trainees", body, null);
        var json = objectMapper.readTree(response.body());
        usernamesByFixtureKey.put(fixtureKey, json.get("username").asText());
    }

    @When("{string} adds a training for trainee {string} on {string} for {int} minutes")
    public void addTraining(String trainerFixtureKey, String traineeFixtureKey, String date, int duration) throws Exception {
        var body = objectMapper.writeValueAsString(Map.of(
                "trainerUsername", resolveUsername(trainerFixtureKey),
                "traineeUsername", resolveUsername(traineeFixtureKey),
                "trainingName", "Integration Test Training",
                "trainingDate", date,
                "trainingDuration", duration));

        lastResponse = post(GYM_CRM_BASE_URL + "/api/trainings", body, jwtToken);
    }

    @Then("the response status should be {int}")
    public void checkStatus(int expectedStatus) {
        assertThat(lastResponse.statusCode()).isEqualTo(expectedStatus);
    }

    @Then("within {int} seconds the report service should show {int} minutes for {string} in year {int} month {int}")
    public void verifyWorkloadSummary(int timeoutSeconds, int expectedMinutes, String trainerFixtureKey,
                                      int year, int month) {
        var trainerUsername = resolveUsername(trainerFixtureKey);

        Awaitility.await()
                .atMost(Duration.ofSeconds(timeoutSeconds))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    var response = get(REPORT_SERVICE_BASE_URL + "/api/trainer-workloads/" + trainerUsername);
                    assertThat(response.statusCode()).isEqualTo(200);

                    var json = objectMapper.readTree(response.body());
                    assertThat(findMonthDuration(json, year, month)).isEqualTo(expectedMinutes);
                });
    }

    private String resolveUsername(String fixtureKey) {
        var username = usernamesByFixtureKey.get(fixtureKey);
        assertThat(username).as("No registered user found for fixture key %s", fixtureKey).isNotNull();
        return username;
    }

    private String[] uniqueNameParts(String fixtureKey) {
        var suffix = UUID.randomUUID().toString().substring(0, 8);
        var names = fixtureKey.split("\\.");
        return new String[]{names[0] + suffix, names[1]};
    }

    private int findMonthDuration(JsonNode summary, int year, int month) {
        for (JsonNode yearNode : summary.get("years")) {
            if (yearNode.get("year").asInt() == year) {
                for (JsonNode monthNode : yearNode.get("months")) {
                    if (monthNode.get("month").asInt() == month) {
                        return monthNode.get("summaryDuration").asInt();
                    }
                }
            }
        }
        throw new AssertionError("No summary found for year=" + year + " month=" + month);
    }

    private HttpResponse<String> post(String url, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String url) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
