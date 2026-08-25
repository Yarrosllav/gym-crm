package com.gym.client;

import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class ReportServiceClientConfig {

    @Bean
    public Request.Options reportServiceRequestOptions() {
        return new Request.Options(
                2000, TimeUnit.MILLISECONDS,
                3000, TimeUnit.MILLISECONDS,
                true);
    }
}
