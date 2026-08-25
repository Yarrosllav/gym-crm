package com.gym.report.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.report.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ServiceAuthFilter extends OncePerRequestFilter {

    private final ServiceJwtValidator validator;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var header = request.getHeader("Authorization");
        var token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;

        if (token == null || !validator.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    new ErrorResponse(401, "Missing or invalid service token", Instant.now()));
            return;
        }

        chain.doFilter(request, response);
    }
}
