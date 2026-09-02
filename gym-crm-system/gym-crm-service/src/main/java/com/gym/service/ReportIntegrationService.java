package com.gym.service;

import com.gym.messaging.TrainerWorkloadMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportIntegrationService {

    private static final String QUEUE = "trainer.workload.queue";

    private final JmsTemplate jmsTemplate;

    public void notifyWorkload(String trainerUsername, String firstName, String lastName, boolean active,
                               LocalDate trainingDate, int trainingDuration,
                               TrainerWorkloadMessage.ActionType actionType) {
        var message = new TrainerWorkloadMessage(
                trainerUsername, firstName, lastName, active, trainingDate, trainingDuration, actionType);
        try {
            jmsTemplate.convertAndSend(QUEUE, message);
            log.info("Workload message sent for trainer={}, action={}", trainerUsername, actionType);
        } catch (Exception ex) {
            log.warn("Failed to send workload message for trainer={}: {}", trainerUsername, ex.getMessage());
        }
    }
}
