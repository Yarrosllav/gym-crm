package com.gym.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@TestConfiguration
@EnableMethodSecurity()
public class MethodSecurityTestConfig {

    @Bean
    public AuthenticationEntryPoint testAuthenticationEntryPoint() {
        return (request, response, authException)
                -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Bean
    public AccessDeniedHandler testAccessDeniedHandler() {
        return (request, response, accessDeniedException)
                -> response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(
            HttpSecurity http,
            @Qualifier("testAuthenticationEntryPoint")
            AuthenticationEntryPoint entryPoint,
            @Qualifier("testAccessDeniedHandler")
            AccessDeniedHandler accessDeniedHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.exceptionHandling(handling -> handling
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/trainees").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/trainers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/training-types").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
