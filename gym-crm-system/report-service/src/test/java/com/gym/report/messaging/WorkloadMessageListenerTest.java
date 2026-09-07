package com.gym.report.messaging;

import com.gym.report.exception.ValidationException;
import com.gym.report.service.TrainerSummaryService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private TrainerSummaryService trainerSummaryService;
    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private Message rawMessage;

    private WorkloadMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorkloadMessageListener(trainerSummaryService, jmsTemplate);
    }

    private TrainerWorkloadMessage buildValidMessage() {
        return new TrainerWorkloadMessage("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.ADD);
    }

    @Test
    void handleWorkloadMessage_shouldProcessValidMessage() throws JMSException {
        when(rawMessage.getStringProperty("transactionId")).thenReturn("tx-123");
        var message = buildValidMessage();

        listener.handleWorkloadMessage(message, rawMessage);

        verify(trainerSummaryService).processWorkloadEvent(message);
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void handleWorkloadMessage_shouldGenerateTransactionId_whenMissingOnMessage() throws JMSException {
        when(rawMessage.getStringProperty("transactionId")).thenReturn(null);
        var message = buildValidMessage();

        listener.handleWorkloadMessage(message, rawMessage);

        verify(trainerSummaryService).processWorkloadEvent(message);
    }

    @Test
    void handleWorkloadMessage_shouldRouteToDlq_whenServiceThrowsValidationException() throws JMSException {
        when(rawMessage.getStringProperty("transactionId")).thenReturn("tx-123");
        var message = buildValidMessage();
        doThrow(new ValidationException("trainerUsername is required"))
                .when(trainerSummaryService).processWorkloadEvent(message);

        listener.handleWorkloadMessage(message, rawMessage);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload.dlq"), eq(message));
    }

    @Test
    void handleWorkloadMessage_shouldPropagateException_forRedelivery_whenRetriesExhausted() throws JMSException {
        when(rawMessage.getStringProperty("transactionId")).thenReturn("tx-123");
        var message = buildValidMessage();
        doThrow(new IllegalStateException("Failed to update trainer summary after 5 retries"))
                .when(trainerSummaryService).processWorkloadEvent(message);

        assertThrows(IllegalStateException.class, () -> listener.handleWorkloadMessage(message, rawMessage));
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void handleWorkloadMessage_shouldPropagateException_forRedeliveryOnTechnicalFailure() throws JMSException {
        when(rawMessage.getStringProperty("transactionId")).thenReturn("tx-123");
        var message = buildValidMessage();
        doThrow(new RuntimeException("DB connection lost"))
                .when(trainerSummaryService).processWorkloadEvent(message);

        assertThrows(RuntimeException.class, () -> listener.handleWorkloadMessage(message, rawMessage));
        verifyNoInteractions(jmsTemplate);
    }
}
