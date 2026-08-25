package com.gym.client;

import com.gym.security.ServiceJwtService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceAuthRequestInterceptor implements RequestInterceptor {

    private final ServiceJwtService serviceJwtService;

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + serviceJwtService.generateServiceToken());
        var transactionId = MDC.get("transactionId");
        if (transactionId != null) {
            template.header("X-Transaction-Id", transactionId);
        }
    }
}
