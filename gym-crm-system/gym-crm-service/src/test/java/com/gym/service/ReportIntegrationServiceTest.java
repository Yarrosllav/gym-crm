package com.gym.service;

import com.gym.messaging.TrainerWorkloadMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportIntegrationServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    private ReportIntegrationService reportIntegrationService;

    @BeforeEach
    void setUp() {
        reportIntegrationService = new ReportIntegrationService(jmsTemplate);
    }

    @Test
    void notifyWorkload_shouldSendMessageToQueue() {
        reportIntegrationService.notifyWorkload("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.ADD);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload.queue"), any(TrainerWorkloadMessage.class));
    }

    @Test
    void notifyWorkload_shouldNotThrow_whenBrokerUnavailable() {
        doThrow(new RuntimeException("Broker unreachable"))
                .when(jmsTemplate).convertAndSend(anyString(), any(TrainerWorkloadMessage.class));

        assertDoesNotThrow(() -> reportIntegrationService.notifyWorkload("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.ADD));
    }

    @Test
    void notifyWorkload_shouldSendCorrectMessageContent() {
        var captor = ArgumentCaptor.forClass(TrainerWorkloadMessage.class);

        reportIntegrationService.notifyWorkload("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.DELETE);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload.queue"), captor.capture());
        var sent = captor.getValue();
        assertDoesNotThrow(() -> {
            assert sent.trainerUsername().equals("Jane.Doe");
            assert sent.actionType() == TrainerWorkloadMessage.ActionType.DELETE;
        });
    }
}
