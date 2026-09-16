package com.gym.cucumber.component;

import com.gym.messaging.TrainerWorkloadMessage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class TrainingSteps {

    private final CucumberSpringConfiguration context;
    private final TestContext testContext;
    private final JmsTemplate jmsTemplate;
    private final TestRestTemplate rest = new TestRestTemplate();

    public TrainingSteps(CucumberSpringConfiguration context, TestContext testContext, JmsTemplate jmsTemplate) {
        this.context = context;
        this.testContext = testContext;
        this.jmsTemplate = jmsTemplate;
    }

    @When("{string} adds a training for trainee {string} on {string} for {int} minutes")
    public void addTraining(String trainerUsername, String traineeUsername, String date, int duration) {
        addTrainingAs(trainerUsername, trainerUsername, traineeUsername, date, duration);
    }

    @When("{string} adds a training as trainer {string} for trainee {string} on {string} for {int} minutes")
    public void addTrainingAs(String actingUser, String trainerUsername, String traineeUsername,
                              String date, int duration) {
        var body = Map.of(
                "trainerUsername", trainerUsername,
                "traineeUsername", traineeUsername,
                "trainingName", "Test Training",
                "trainingDate", date,
                "trainingDuration", duration);

        var headers = new HttpHeaders();
        headers.setBearerAuth(testContext.getToken(actingUser));
        headers.setContentType(MediaType.APPLICATION_JSON);

        testContext.setLastResponse(rest.exchange(
                context.baseUrl() + "/api/trainings",
                HttpMethod.POST, new HttpEntity<>(body, headers), String.class));
    }

    @Then("a workload event should be published for trainer {string}")
    public void verifyWorkloadEventPublished(String trainerUsername) {
        var captor = ArgumentCaptor.forClass(Object.class);
        verify(jmsTemplate, atLeastOnce()).convertAndSend(anyString(), captor.capture(), any(MessagePostProcessor.class));

        var published = captor.getAllValues().stream()
                .filter(TrainerWorkloadMessage.class::isInstance)
                .map(TrainerWorkloadMessage.class::cast)
                .anyMatch(msg -> msg.trainerUsername().equals(trainerUsername));

        assertThat(published)
                .as("Expected a workload event published for trainer %s", trainerUsername)
                .isTrue();
    }
}
