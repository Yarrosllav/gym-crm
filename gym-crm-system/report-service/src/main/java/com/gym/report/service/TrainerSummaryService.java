package com.gym.report.service;

import com.gym.report.dao.TrainerTrainingSummaryRepository;
import com.gym.report.dto.MonthSummaryResponse;
import com.gym.report.dto.TrainerWorkloadSummaryResponse;
import com.gym.report.dto.YearSummaryResponse;
import com.gym.report.exception.EntityNotFoundException;
import com.gym.report.exception.ValidationException;
import com.gym.report.messaging.TrainerWorkloadMessage;
import com.gym.report.model.MonthSummary;
import com.gym.report.model.TrainerTrainingSummary;
import com.gym.report.model.YearSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerSummaryService {

    private static final int MAX_RETRIES = 5;

    private final TrainerTrainingSummaryRepository repository;

    public void processWorkloadEvent(TrainerWorkloadMessage message) {
        validate(message);

        var year = message.trainingDate().getYear();
        var month = message.trainingDate().getMonthValue();
        var delta = message.actionType() == TrainerWorkloadMessage.ActionType.ADD
                ? message.trainingDuration()
                : -message.trainingDuration();

        log.info("Processing workload event: trainer={}, year={}, month={}, action={}",
                message.trainerUsername(), year, month, message.actionType());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                var summary = repository.findByTrainerUsername(message.trainerUsername())
                        .orElseGet(() -> createNewSummary(message));

                applyDelta(summary, year, month, delta, message);
                repository.save(summary);

                log.info("Trainer summary saved for username={}", message.trainerUsername());
                return;
            } catch (OptimisticLockingFailureException ex) {
                log.warn("Concurrent update detected for trainer={}, retry {}/{}",
                        message.trainerUsername(), attempt, MAX_RETRIES);
            }
        }

        throw new IllegalStateException("Failed to update trainer summary after " + MAX_RETRIES + " retries: "
                + message.trainerUsername());
    }

    private TrainerTrainingSummary createNewSummary(TrainerWorkloadMessage message) {
        log.debug("No existing summary found for trainer={}, creating new document", message.trainerUsername());
        var summary = new TrainerTrainingSummary();
        summary.setTrainerUsername(message.trainerUsername());
        summary.setFirstName(message.firstName());
        summary.setLastName(message.lastName());
        summary.setTrainerStatus(message.isActive());
        return summary;
    }

    public TrainerWorkloadSummaryResponse getSummary(String trainerUsername) {
        log.info("Fetching training summary for trainer={}", trainerUsername);

        var summary = repository.findByTrainerUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("No workload data found for trainer: " + trainerUsername));

        var years = summary.getYears().stream()
                .map(y -> new YearSummaryResponse(
                        y.getYear(),
                        y.getMonths().stream()
                                .map(m -> new MonthSummaryResponse(m.getMonth(), m.getTrainingsSummaryDuration()))
                                .sorted(Comparator.comparingInt(MonthSummaryResponse::month))
                                .toList()))
                .sorted(Comparator.comparingInt(YearSummaryResponse::year))
                .toList();

        return new TrainerWorkloadSummaryResponse(
                summary.getTrainerUsername(), summary.getFirstName(), summary.getLastName(),
                summary.isTrainerStatus(), years);
    }

    private void applyDelta(TrainerTrainingSummary summary, int year, int month, int delta,
                            TrainerWorkloadMessage message) {
        summary.setFirstName(message.firstName());
        summary.setLastName(message.lastName());
        summary.setTrainerStatus(message.isActive());

        var yearSummary = summary.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    var newYear = new YearSummary();
                    newYear.setYear(year);
                    summary.getYears().add(newYear);
                    return newYear;
                });

        var monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    var newMonth = new MonthSummary();
                    newMonth.setMonth(month);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        var updated = Math.max(0, monthSummary.getTrainingsSummaryDuration() + delta);
        log.debug("Updating duration for trainer={}, {}/{}: {} -> {}",
                summary.getTrainerUsername(), year, month, monthSummary.getTrainingsSummaryDuration(), updated);
        monthSummary.setTrainingsSummaryDuration(updated);
    }

    private void validate(TrainerWorkloadMessage message) {
        if (message.trainerUsername() == null || message.trainerUsername().isBlank()) {
            throw new ValidationException("trainerUsername is required");
        }
        if (message.firstName() == null || message.firstName().isBlank()) {
            throw new ValidationException("firstName is required");
        }
        if (message.lastName() == null || message.lastName().isBlank()) {
            throw new ValidationException("lastName is required");
        }
        if (message.isActive() == null) {
            throw new ValidationException("isActive is required");
        }
        if (message.trainingDate() == null) {
            throw new ValidationException("trainingDate is required");
        }
        if (message.trainingDuration() == null || message.trainingDuration() <= 0) {
            throw new ValidationException("trainingDuration must be a positive number");
        }
        if (message.actionType() == null) {
            throw new ValidationException("actionType is required");
        }
    }
}
