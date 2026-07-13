package com.gym.service;

import com.gym.dao.IUserDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameGenerator {

    private final IUserDao userDao;

    public String generate(String firstName, String lastName) {
        log.debug("Generating username for: {} {}", firstName, lastName);

        var base = firstName + "." + lastName;
        var candidate = base;
        var counter = 1;

        while (userDao.existsByUsername(candidate)) {
            candidate = base + counter;
            counter++;
        }

        log.debug("Generated username: {}", candidate);
        return candidate;
    }
}
