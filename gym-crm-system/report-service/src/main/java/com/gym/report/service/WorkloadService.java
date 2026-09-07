package com.gym.report.service;

import com.gym.report.dao.WorkloadRepository;
import com.gym.report.dto.MonthSummary;
import com.gym.report.dto.TrainerWorkloadSummaryResponse;
import com.gym.report.dto.YearSummary;
import com.gym.report.exception.EntityNotFoundException;
import com.gym.report.messaging.TrainerWorkloadMessage;
import com.gym.report.model.TrainerMonthlyWorkload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadService {

    private final WorkloadRepository workloadRepository;

    @Transactional
    public void applyWorkload(TrainerWorkloadMessage message) {
        log.info("Applying workload: trainer={}, date={}, duration={}, action={}",
                message.trainerUsername(), message.trainingDate(), message.trainingDuration(), message.actionType());

        var year = message.trainingDate().getYear();
        var month = message.trainingDate().getMonthValue();
        var existing = workloadRepository.find(message.trainerUsername(), year, month);

        if (existing.isPresent()) {
            var workload = existing.get();
            workload.setFirstName(message.firstName());
            workload.setLastName(message.lastName());
            workload.setActive(message.isActive());
            workload.setTotalDurationMinutes(applyDelta(workload.getTotalDurationMinutes(), message));
            workloadRepository.update(workload);
        } else if (message.actionType() == TrainerWorkloadMessage.ActionType.DELETE) {
            log.warn("Delete requested for non-existing workload record: trainer={}, year={}, month={}",
                    message.trainerUsername(), year, month);
            return;
        } else {
            var workload = new TrainerMonthlyWorkload();
            workload.setTrainerUsername(message.trainerUsername());
            workload.setFirstName(message.firstName());
            workload.setLastName(message.lastName());
            workload.setActive(message.isActive());
            workload.setYear(year);
            workload.setMonth(month);
            workload.setTotalDurationMinutes(message.trainingDuration());
            workloadRepository.create(workload);
        }

        log.info("Workload applied successfully for trainer={}", message.trainerUsername());
    }

    private int applyDelta(int current, TrainerWorkloadMessage message) {
        return message.actionType() == TrainerWorkloadMessage.ActionType.ADD
                ? current + message.trainingDuration()
                : Math.max(0, current - message.trainingDuration());
    }

    @Transactional(readOnly = true)
    public TrainerWorkloadSummaryResponse getSummary(String trainerUsername) {
        var records = workloadRepository.findAllByUsername(trainerUsername);
        if (records.isEmpty()) {
            throw new EntityNotFoundException("No workload data found for trainer: " + trainerUsername);
        }

        var first = records.getFirst();
        var years = records.stream()
                .collect(Collectors.groupingBy(TrainerMonthlyWorkload::getYear))
                .entrySet().stream()
                .map(entry -> new YearSummary(entry.getKey(),
                        entry.getValue().stream()
                                .map(workload -> new MonthSummary(workload.getMonth(), workload.getTotalDurationMinutes()))
                                .sorted(Comparator.comparingInt(MonthSummary::month))
                                .toList()))
                .sorted(Comparator.comparingInt(YearSummary::year))
                .toList();

        return new TrainerWorkloadSummaryResponse(
                first.getTrainerUsername(), first.getFirstName(), first.getLastName(), first.isActive(), years);
    }
}
