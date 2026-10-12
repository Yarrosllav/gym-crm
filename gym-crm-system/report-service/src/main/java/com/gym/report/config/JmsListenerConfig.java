package com.gym.report.config;

import jakarta.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;

@Slf4j
@Configuration
public class JmsListenerConfig {

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter,
            @Value("${spring.jms.listener.auto-startup:true}") boolean autoStartup) {
        var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrency("3-10");
        factory.setErrorHandler(throwable ->
                log.error("Unhandled error while processing JMS message", throwable));
        factory.setAutoStartup(autoStartup);
        return factory;
    }
}
