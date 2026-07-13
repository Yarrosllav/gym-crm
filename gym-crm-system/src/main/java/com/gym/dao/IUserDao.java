package com.gym.dao;

import com.gym.model.User;

public interface IUserDao extends ICreateAndReadDao<User, Long> {
    boolean existsByUsername(String username);
}
