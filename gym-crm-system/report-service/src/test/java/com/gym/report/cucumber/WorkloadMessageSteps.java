package com.gym.report.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.report.messaging.TrainerWorkloadMessage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadMessageSteps {

    private final JmsTemplate jmsTemplate;
    private final TestContext testContext;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${messaging.queue.trainer-workload:trainer.workload.queue}")
    private String queueName;

    @Value("${messaging.queue.trainer-workload-dlq:trainer.workload.dlq}")
    private String dlqName;

    public WorkloadMessageSteps(JmsTemplate jmsTemplate, TestContext testContext) {
        this.jmsTemplate = jmsTemplate;
        this.testContext = testContext;
    }

    @When("a workload event is published for trainer {string} on {string} for {int} minutes with action {string}")
    public void publishEvent(String trainerUsername, String date, int duration, String action) {
        testContext.currentTrainerUsername = trainerUsername;
        var names = trainerUsername.split("\\.");
        var message = new TrainerWorkloadMessage(trainerUsername, names[0], names[1], true,
                LocalDate.parse(date), duration, TrainerWorkloadMessage.ActionType.valueOf(action));
        jmsTemplate.convertAndSend(queueName, message);
    }

    @When("{int} workload events are published concurrently for trainer {string} on {string} for {int} minutes each with action {string}")
    public void publishConcurrentEvents(int count, String trainerUsername, String date, int duration, String action) {
        testContext.currentTrainerUsername = trainerUsername;
        var names = trainerUsername.split("\\.");
        var futures = new CompletableFuture[count];
        for (int i = 0; i < count; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                var message = new TrainerWorkloadMessage(trainerUsername, names[0], names[1], true,
                        LocalDate.parse(date), duration, TrainerWorkloadMessage.ActionType.valueOf(action));
                jmsTemplate.convertAndSend(queueName, message);
            });
        }
        CompletableFuture.allOf(futures).join();
    }

    @When("an invalid workload event with a blank trainer username is published")
    public void publishInvalidEvent() {
        var message = new TrainerWorkloadMessage(" ", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.ADD);
        jmsTemplate.convertAndSend(queueName, message);
    }

    @Then("within {int} seconds the message should be routed to the dead letter queue")
    public void verifyMessageInDlq(int timeoutSeconds) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(timeoutSeconds))
                .untilAsserted(() -> {
                    var received = jmsTemplate.receive(dlqName);
                    assertThat(received).as("Expected a message in the DLQ").isNotNull();
                });
    }
}
