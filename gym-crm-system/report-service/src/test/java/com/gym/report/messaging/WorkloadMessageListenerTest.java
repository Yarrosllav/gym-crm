package com.gym.report.messaging;

import com.gym.report.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private WorkloadService workloadService;
    @Mock
    private JmsTemplate jmsTemplate;

    private WorkloadMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorkloadMessageListener(workloadService, jmsTemplate);
    }

    private TrainerWorkloadMessage buildValidMessage() {
        return new TrainerWorkloadMessage("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.ADD);
    }

    @Test
    void handleWorkloadMessage_shouldProcessValidMessage() {
        listener.handleWorkloadMessage(buildValidMessage());

        verify(workloadService).applyWorkload(buildValidMessage());
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void handleWorkloadMessage_shouldRouteToDlq_whenTrainerUsernameMissing() {
        var invalid = new TrainerWorkloadMessage(" ", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.ADD);

        listener.handleWorkloadMessage(invalid);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload.dlq"), eq(invalid));
        verifyNoInteractions(workloadService);
    }

    @Test
    void handleWorkloadMessage_shouldRouteToDlq_whenTrainingDateMissing() {
        var invalid = new TrainerWorkloadMessage("Jane.Doe", "Jane", "Doe", true,
                null, 60, TrainerWorkloadMessage.ActionType.ADD);

        listener.handleWorkloadMessage(invalid);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload.dlq"), any(TrainerWorkloadMessage.class));
        verifyNoInteractions(workloadService);
    }

    @Test
    void handleWorkloadMessage_shouldRouteToDlq_whenActionTypeMissing() {
        var invalid = new TrainerWorkloadMessage("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, null);

        listener.handleWorkloadMessage(invalid);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload.dlq"), any(TrainerWorkloadMessage.class));
    }

    @Test
    void handleWorkloadMessage_shouldPropagateException_forRedeliveryOnTechnicalFailure() {
        doThrow(new RuntimeException("DB connection lost")).when(workloadService).applyWorkload(any());

        assertThrows(RuntimeException.class, () -> listener.handleWorkloadMessage(buildValidMessage()));
        verifyNoInteractions(jmsTemplate);
    }
}
