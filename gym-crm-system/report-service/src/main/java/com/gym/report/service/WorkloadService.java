package com.gym.report.service;

import com.gym.report.dao.WorkloadRepository;
import com.gym.report.dto.*;
import com.gym.report.exception.EntityNotFoundException;
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
    public void applyWorkload(TrainerWorkloadRequest request) {
        log.info("Applying workload: trainer={}, date={}, duration={}, action={}",
                request.trainerUsername(), request.trainingDate(), request.trainingDuration(), request.actionType());

        var year = request.trainingDate().getYear();
        var month = request.trainingDate().getMonthValue();
        var existing = workloadRepository.find(request.trainerUsername(), year, month);

        if (existing.isPresent()) {
            var workload = existing.get();
            workload.setFirstName(request.firstName());
            workload.setLastName(request.lastName());
            workload.setActive(request.isActive());
            workload.setTotalDurationMinutes(applyDelta(workload.getTotalDurationMinutes(), request));
            workloadRepository.update(workload);
        } else if (request.actionType() == ActionType.DELETE) {
            log.warn("Delete requested for non-existing workload record: trainer={}, year={}, month={}",
                    request.trainerUsername(), year, month);
            return;
        } else {
            var workload = new TrainerMonthlyWorkload();
            workload.setTrainerUsername(request.trainerUsername());
            workload.setFirstName(request.firstName());
            workload.setLastName(request.lastName());
            workload.setActive(request.isActive());
            workload.setYear(year);
            workload.setMonth(month);
            workload.setTotalDurationMinutes(request.trainingDuration());
            workloadRepository.create(workload);
        }

        log.info("Workload applied successfully for trainer={}", request.trainerUsername());
    }

    private int applyDelta(int current, TrainerWorkloadRequest request) {
        return request.actionType() == ActionType.ADD
                ? current + request.trainingDuration()
                : Math.max(0, current - request.trainingDuration());
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
