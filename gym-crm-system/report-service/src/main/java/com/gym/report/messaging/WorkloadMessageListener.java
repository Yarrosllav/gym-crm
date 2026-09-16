package com.gym.report.messaging;

import com.gym.report.exception.ValidationException;
import com.gym.report.service.TrainerSummaryService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageListener {

    private static final String DLQ = "trainer.workload.dlq";
    private static final String TRANSACTION_ID = "transactionId";

    private final TrainerSummaryService trainerSummaryService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "trainer.workload.queue", containerFactory = "jmsListenerContainerFactory")
    public void handleWorkloadMessage(TrainerWorkloadMessage message, Message rawMessage) throws JMSException {
        var transactionId = rawMessage.getStringProperty(TRANSACTION_ID);
        MDC.put(TRANSACTION_ID, transactionId != null ? transactionId : UUID.randomUUID().toString());

        try {
            log.info("Received workload message: trainer={}, action={}",
                    message.trainerUsername(), message.actionType());

            trainerSummaryService.processWorkloadEvent(message);

            log.info("Workload message processed successfully for trainer={}", message.trainerUsername());
        } catch (ValidationException ex) {
            log.warn("Invalid workload message, routing to DLQ: {}", ex.getMessage());
            jmsTemplate.convertAndSend(DLQ, message);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}
