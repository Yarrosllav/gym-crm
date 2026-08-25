package com.gym.service;

import com.gym.dao.IProfileDao;
import com.gym.exception.EntityNotFoundException;
import com.gym.exception.ValidationException;
import com.gym.model.IHasUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Optional;

@Slf4j
public abstract class AbstractProfileService<T extends IHasUser, ID extends Serializable>
        extends AbstractService<T, ID> {

    protected final PasswordEncoder passwordEncoder;

    private final IProfileDao<T, ID> dao;

    protected AbstractProfileService(IProfileDao<T, ID> dao, PasswordEncoder passwordEncoder) {
        super(dao);
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    protected abstract Optional<T> findByUsername(String username);

    protected abstract Optional<T> findByUsernameWithProfile(String username);

    @Transactional
    public void changePassword(String username, String newPassword) {
        log.info("Changing password for: {}", username);

        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password is required");
        }

        var profile = getProfileLazy(username);
        profile.getUser().setPassword(passwordEncoder.encode(newPassword));
        dao.update(profile);
        log.info("Password changed for: {}", username);
    }

    @Transactional
    public void setActive(String username, boolean active) {
        log.info("Setting active status for {} to {}", username, active);

        var profile = getProfileLazy(username);
        var user = profile.getUser();

        if (user.isActive() == active) {
            log.warn("{} is already {}", username, active ? "active" : "inactive");
            throw new ValidationException("Profile is already " + (active ? "active" : "inactive"));
        }

        user.setActive(active);
        dao.update(profile);
        log.info("{} active status set to {}", username, active);
    }

    @Transactional(readOnly = true)
    public T getProfile(String username) {
        log.info("Getting profile for: {}", username);
        return findByUsernameWithProfile(username)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + username));
    }

    protected void update(T entity) {
        dao.update(entity);
    }

    private T getProfileLazy(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + username));

    }
}
