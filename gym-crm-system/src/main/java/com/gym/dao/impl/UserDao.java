package com.gym.dao.impl;

import com.gym.dao.AbstractDao;
import com.gym.dao.IUserDao;
import com.gym.model.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class UserDao extends AbstractDao<User, Long> implements IUserDao {

    public UserDao(SessionFactory sessionFactory) {
        super(sessionFactory, User.class);
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("Checking if username '{}' exists", username);
        var query = session().createNativeQuery(
                "SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)", Boolean.class
        );
        query.setParameter("username", username);

        Boolean exists = query.uniqueResult();

        log.debug("Username '{}' exists: {}", username, exists != null && exists);
        return exists != null && exists;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        log.debug("Fetching User by username: {}", username);
        return session()
                .createQuery("from User u where u.username = :username", User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}
