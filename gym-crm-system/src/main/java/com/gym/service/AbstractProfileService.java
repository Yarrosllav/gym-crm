package com.gym.service;

import com.gym.dao.IProfileDao;
import com.gym.exception.AuthenticationException;
import com.gym.exception.ValidationException;
import com.gym.model.IHasUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Optional;

@Slf4j
public abstract class AbstractProfileService<T extends IHasUser, ID extends Serializable>
        extends AbstractService<T, ID> {

    private final IProfileDao<T, ID> dao;

    protected AbstractProfileService(IProfileDao<T, ID> dao) {
        super(dao);
        this.dao = dao;
    }

    protected abstract Optional<T> findByUsername(String username);

    @Transactional(readOnly = true)
    public boolean matchCredentials(String username, String password) {
        var matches = findByUsername(username)
                .map(profile -> profile.getUser().getPassword().equals(password))
                .orElse(false);
        log.debug("Credentials match check for '{}': {}", username, matches);
        return matches;
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for: {}", username);

        var profile = authenticate(username, oldPassword);
        if (newPassword == null || newPassword.isBlank()) {
            throw new ValidationException("New password is required");
        }
        profile.getUser().setPassword(newPassword);
        dao.update(profile);
        log.info("Password changed for: {}", username);
    }

    @Transactional
    public void toggleActiveStatus(String username, String password) {
        log.info("Toggling status for user: {}", username);

        var profile = authenticate(username, password);
        var user = profile.getUser();

        boolean newStatus = !user.isActive();
        user.setActive(newStatus);

        dao.update(profile);
        log.info("Status for user {} toggled to {}", username, newStatus ? "ACTIVE" : "INACTIVE");
    }

    @Transactional(readOnly = true)
    public T getProfile(String username, String password) {
        log.info("Getting profile for: {}", username);
        return authenticate(username, password);
    }

    protected T authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);

        var profile = findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Profile not found: " + username));
        if (!profile.getUser().getPassword().equals(password)) {
            log.warn("Authentication failed for: {}", username);
            throw new AuthenticationException("Invalid password for user: " + username);
        }
        return profile;
    }

    protected void update(T entity) {
        dao.update(entity);
    }
}
