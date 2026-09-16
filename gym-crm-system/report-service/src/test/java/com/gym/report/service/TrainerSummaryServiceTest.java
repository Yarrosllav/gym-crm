package com.gym.report.service;

import com.gym.report.dao.TrainerTrainingSummaryRepository;
import com.gym.report.exception.EntityNotFoundException;
import com.gym.report.exception.ValidationException;
import com.gym.report.messaging.TrainerWorkloadMessage;
import com.gym.report.model.MonthSummary;
import com.gym.report.model.TrainerTrainingSummary;
import com.gym.report.model.YearSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerSummaryServiceTest {

    @Mock
    private TrainerTrainingSummaryRepository repository;

    private TrainerSummaryService trainerSummaryService;

    @BeforeEach
    void setUp() {
        trainerSummaryService = new TrainerSummaryService(repository);
    }

    private TrainerWorkloadMessage buildMessage(TrainerWorkloadMessage.ActionType action, int duration) {
        return new TrainerWorkloadMessage("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), duration, action);
    }

    @Test
    void processWorkloadEvent_shouldCreateNewSummary_whenNoneExistsAndActionIsAdd() {
        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.empty());

        trainerSummaryService.processWorkloadEvent(buildMessage(TrainerWorkloadMessage.ActionType.ADD, 60));

        var captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(repository).save(captor.capture());

        var saved = captor.getValue();
        assertEquals("Jane.Doe", saved.getTrainerUsername());
        assertEquals(1, saved.getYears().size());
        assertEquals(2026, saved.getYears().getFirst().getYear());
        assertEquals(1, saved.getYears().getFirst().getMonths().size());
        assertEquals(3, saved.getYears().getFirst().getMonths().getFirst().getMonth());
        assertEquals(60, saved.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration());
    }

    @Test
    void processWorkloadEvent_shouldIncrementExistingMonth_whenActionIsAdd() {
        var existing = existingSummaryWithDuration(2026, 3, 100);
        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.of(existing));

        trainerSummaryService.processWorkloadEvent(buildMessage(TrainerWorkloadMessage.ActionType.ADD, 60));

        assertEquals(160, existing.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration());
        verify(repository).save(existing);
    }

    @Test
    void processWorkloadEvent_shouldDecrementExistingMonth_whenActionIsDelete() {
        var existing = existingSummaryWithDuration(2026, 3, 100);
        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.of(existing));

        trainerSummaryService.processWorkloadEvent(buildMessage(TrainerWorkloadMessage.ActionType.DELETE, 60));

        assertEquals(40, existing.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration());
    }

    @Test
    void processWorkloadEvent_shouldNotGoBelowZero_whenDeletingMoreThanRecorded() {
        var existing = existingSummaryWithDuration(2026, 3, 30);
        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.of(existing));

        trainerSummaryService.processWorkloadEvent(buildMessage(TrainerWorkloadMessage.ActionType.DELETE, 60));

        assertEquals(0, existing.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration());
    }

    @Test
    void processWorkloadEvent_shouldAddNewMonth_toExistingYear() {
        var existing = existingSummaryWithDuration(2026, 3, 60);
        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.of(existing));

        var aprilMessage = new TrainerWorkloadMessage("Jane.Doe", "Jane", "Doe", true,
                LocalDate.of(2026, 4, 10), 45, TrainerWorkloadMessage.ActionType.ADD);
        trainerSummaryService.processWorkloadEvent(aprilMessage);

        assertEquals(2, existing.getYears().getFirst().getMonths().size());
    }

    @Test
    void processWorkloadEvent_shouldThrow_whenTrainerUsernameMissing() {
        var invalid = new TrainerWorkloadMessage(" ", "Jane", "Doe", true,
                LocalDate.of(2026, 3, 15), 60, TrainerWorkloadMessage.ActionType.ADD);

        assertThrows(ValidationException.class, () -> trainerSummaryService.processWorkloadEvent(invalid));
        verifyNoInteractions(repository);
    }

    @Test
    void processWorkloadEvent_shouldThrow_whenDurationNotPositive() {
        var invalid = buildMessage(TrainerWorkloadMessage.ActionType.ADD, 0);

        assertThrows(ValidationException.class, () -> trainerSummaryService.processWorkloadEvent(invalid));
        verifyNoInteractions(repository);
    }

    @Test
    void processWorkloadEvent_shouldRetry_whenOptimisticLockingFailureOccursOnce() {
        var existing = existingSummaryWithDuration(2026, 3, 100);
        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.of(existing));
        when(repository.save(any()))
                .thenThrow(new OptimisticLockingFailureException("conflict"))
                .thenReturn(existing);

        trainerSummaryService.processWorkloadEvent(buildMessage(TrainerWorkloadMessage.ActionType.ADD, 60));

        verify(repository, times(2)).save(any());
    }

    @Test
    void processWorkloadEvent_shouldThrowIllegalState_whenRetriesExhausted() {
        var existing = existingSummaryWithDuration(2026, 3, 100);
        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThrows(IllegalStateException.class,
                () -> trainerSummaryService.processWorkloadEvent(buildMessage(TrainerWorkloadMessage.ActionType.ADD, 60)));

        verify(repository, times(5)).save(any());
    }

    @Test
    void getSummary_shouldReturnSortedYearsAndMonths() {
        var summary = new TrainerTrainingSummary();
        summary.setTrainerUsername("Jane.Doe");
        summary.setFirstName("Jane");
        summary.setLastName("Doe");
        summary.setTrainerStatus(true);

        var year2026 = new YearSummary();
        year2026.setYear(2026);
        var april = new MonthSummary();
        april.setMonth(4);
        april.setTrainingsSummaryDuration(90);
        var march = new MonthSummary();
        march.setMonth(3);
        march.setTrainingsSummaryDuration(60);
        year2026.getMonths().addAll(List.of(april, march));
        summary.getYears().add(year2026);

        when(repository.findByTrainerUsername("Jane.Doe")).thenReturn(Optional.of(summary));

        var result = trainerSummaryService.getSummary("Jane.Doe");

        assertEquals(1, result.years().size());
        assertEquals(2, result.years().getFirst().months().size());
        assertEquals(3, result.years().getFirst().months().getFirst().month());
        assertEquals(4, result.years().getFirst().months().get(1).month());
    }

    @Test
    void getSummary_shouldThrow_whenNoDataFound() {
        when(repository.findByTrainerUsername("Unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerSummaryService.getSummary("Unknown"));
    }

    private TrainerTrainingSummary existingSummaryWithDuration(int year, int month, int duration) {
        var summary = new TrainerTrainingSummary();
        summary.setTrainerUsername("Jane.Doe");
        summary.setFirstName("Jane");
        summary.setLastName("Doe");
        summary.setTrainerStatus(true);

        var yearSummary = new YearSummary();
        yearSummary.setYear(year);
        var monthSummary = new MonthSummary();
        monthSummary.setMonth(month);
        monthSummary.setTrainingsSummaryDuration(duration);
        yearSummary.getMonths().add(monthSummary);
        summary.getYears().add(yearSummary);

        return summary;
    }
}
