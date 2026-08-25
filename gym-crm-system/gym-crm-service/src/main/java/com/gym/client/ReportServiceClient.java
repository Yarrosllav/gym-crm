package com.gym.client;

import com.gym.dto.request.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "report-service", configuration = ReportServiceClientConfig.class)
public interface ReportServiceClient {

    @PostMapping("/api/trainer-workloads")
    void applyWorkload(@RequestBody TrainerWorkloadRequest request);
}
