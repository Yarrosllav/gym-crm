package com.gym.report.service;

import com.gym.report.dao.WorkloadRepository;
import com.gym.report.exception.EntityNotFoundException;
import com.gym.report.messaging.TrainerWorkloadMessage;
import com.gym.report.model.TrainerMonthlyWorkload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceTest {

    @Mock
    private WorkloadRepository workloadRepository;

    private WorkloadService workloadService;

    @BeforeEach
    void setUp() {
        workloadService = new WorkloadService(workloadRepository);
    }

    private TrainerWorkloadMessage buildMessage(TrainerWorkloadMessage.ActionType action, int duration) {
        return new TrainerWorkloadMessage("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), duration, action);
    }

    @Test
    void applyWorkload_shouldCreateNewRecord_whenNoneExistsAndActionIsAdd() {
        when(workloadRepository.find("Jane.Doe", 2026, 3)).thenReturn(Optional.empty());

        workloadService.applyWorkload(buildMessage(TrainerWorkloadMessage.ActionType.ADD, 60));

        var captor = org.mockito.ArgumentCaptor.forClass(TrainerMonthlyWorkload.class);
        verify(workloadRepository).create(captor.capture());
        assertEquals(60, captor.getValue().getTotalDurationMinutes());
    }

    @Test
    void applyWorkload_shouldIncrementExistingRecord_whenActionIsAdd() {
        var existing = new TrainerMonthlyWorkload();
        existing.setTotalDurationMinutes(100);
        when(workloadRepository.find("Jane.Doe", 2026, 3)).thenReturn(Optional.of(existing));

        workloadService.applyWorkload(buildMessage(TrainerWorkloadMessage.ActionType.ADD, 60));

        assertEquals(160, existing.getTotalDurationMinutes());
        verify(workloadRepository).update(existing);
    }

    @Test
    void applyWorkload_shouldDecrementExistingRecord_whenActionIsDelete() {
        var existing = new TrainerMonthlyWorkload();
        existing.setTotalDurationMinutes(100);
        when(workloadRepository.find("Jane.Doe", 2026, 3)).thenReturn(Optional.of(existing));

        workloadService.applyWorkload(buildMessage(TrainerWorkloadMessage.ActionType.DELETE, 60));

        assertEquals(40, existing.getTotalDurationMinutes());
    }

    @Test
    void applyWorkload_shouldNotGoBelowZero_whenDeletingMoreThanRecorded() {
        var existing = new TrainerMonthlyWorkload();
        existing.setTotalDurationMinutes(30);
        when(workloadRepository.find("Jane.Doe", 2026, 3)).thenReturn(Optional.of(existing));

        workloadService.applyWorkload(buildMessage(TrainerWorkloadMessage.ActionType.DELETE, 60));

        assertEquals(0, existing.getTotalDurationMinutes());
    }

    @Test
    void applyWorkload_shouldIgnoreDelete_whenNoRecordExists() {
        when(workloadRepository.find("Jane.Doe", 2026, 3)).thenReturn(Optional.empty());

        workloadService.applyWorkload(buildMessage(TrainerWorkloadMessage.ActionType.DELETE, 60));

        verify(workloadRepository, never()).create(any());
        verify(workloadRepository, never()).update(any());
    }

    @Test
    void getSummary_shouldGroupByYearAndMonth() {
        var w1 = new TrainerMonthlyWorkload();
        w1.setTrainerUsername("Jane.Doe");
        w1.setYear(2026);
        w1.setMonth(3);
        w1.setTotalDurationMinutes(60);

        var w2 = new TrainerMonthlyWorkload();
        w2.setTrainerUsername("Jane.Doe");
        w2.setYear(2026);
        w2.setMonth(4);
        w2.setTotalDurationMinutes(90);

        when(workloadRepository.findAllByUsername("Jane.Doe")).thenReturn(List.of(w1, w2));

        var summary = workloadService.getSummary("Jane.Doe");

        assertEquals(1, summary.years().size());
        assertEquals(2, summary.years().get(0).months().size());
    }

    @Test
    void getSummary_shouldThrow_whenNoDataFound() {
        when(workloadRepository.findAllByUsername("Unknown")).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> workloadService.getSummary("Unknown"));
    }
}
