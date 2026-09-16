package com.gym.report.cucumber;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadSummarySteps {

    private final CucumberSpringConfiguration context;
    private final TestContext testContext;
    private final TestRestTemplate rest = new TestRestTemplate();

    public WorkloadSummarySteps(CucumberSpringConfiguration context, TestContext testContext) {
        this.context = context;
        this.testContext = testContext;
    }

    @When("the summary for trainer {string} is requested")
    public void requestSummary(String trainerUsername) {
        testContext.lastResponse = rest.getForEntity(
                context.baseUrl() + "/api/trainer-workloads/" + trainerUsername, String.class);
    }

    @Then("the response status should be {int}")
    public void checkStatus(int expectedStatus) {
        assertThat(testContext.lastResponse.getStatusCode().value())
                .as("Response body: %s", testContext.lastResponse.getBody())
                .isEqualTo(expectedStatus);
    }

    @Then("within {int} seconds the summary should contain {int} minutes for year {int} month {int}")
    public void awaitAndCheckDuration(int timeoutSeconds, int expectedMinutes, int year, int month) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(timeoutSeconds))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    requestSummary(currentTrainerUsername());
                    assertThat(testContext.lastResponse.getStatusCode().value()).isEqualTo(200);
                    assertThat(extractDuration(testContext.lastResponse.getBody(), year, month))
                            .isEqualTo(expectedMinutes);
                });
    }

    private String currentTrainerUsername() {
        return testContext.currentTrainerUsername;
    }

    private int extractDuration(String responseBody, int year, int month) throws Exception {
        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
        for (var yearNode : json.get("years")) {
            if (yearNode.get("year").asInt() == year) {
                for (var monthNode : yearNode.get("months")) {
                    if (monthNode.get("month").asInt() == month) {
                        return monthNode.get("summaryDuration").asInt();
                    }
                }
            }
        }
        throw new AssertionError("No entry found for year=" + year + " month=" + month);
    }
}
