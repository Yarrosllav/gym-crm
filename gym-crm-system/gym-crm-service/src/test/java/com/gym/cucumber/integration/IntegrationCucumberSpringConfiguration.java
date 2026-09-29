package com.gym.cucumber.integration;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = IntegrationCucumberSpringConfiguration.EmptyIntegrationContext.class)
public class IntegrationCucumberSpringConfiguration {

    @Configuration
    static class EmptyIntegrationContext {
    }
}
