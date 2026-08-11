package com.gym.dao;

import com.gym.model.User;

import java.util.Optional;

public interface IUserDao extends ICreateAndReadDao<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
