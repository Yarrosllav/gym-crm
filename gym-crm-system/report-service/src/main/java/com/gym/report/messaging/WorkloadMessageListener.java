package com.gym.report.messaging;

import com.gym.report.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageListener {

    private static final String DLQ = "trainer.workload.dlq";

    private final WorkloadService workloadService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "trainer.workload.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleWorkloadMessage(TrainerWorkloadMessage message) {
        log.info("Received workload message: trainer={}, action={}", message.trainerUsername(), message.actionType());

        var validationError = validate(message);
        if (validationError != null) {
            log.warn("Invalid workload message, routing to DLQ: {}", validationError);
            jmsTemplate.convertAndSend(DLQ, message);
            return;
        }

        workloadService.applyWorkload(message);
        log.info("Workload message processed successfully for trainer={}", message.trainerUsername());
    }

    private String validate(TrainerWorkloadMessage message) {
        if (message.trainerUsername() == null || message.trainerUsername().isBlank()) {
            return "trainerUsername is missing";
        }
        if (message.trainingDate() == null) {
            return "trainingDate is missing";
        }
        if (message.trainingDuration() == null) {
            return "trainingDuration is missing";
        }
        if (message.actionType() == null) {
            return "actionType is missing";
        }
        return null;
    }
}
