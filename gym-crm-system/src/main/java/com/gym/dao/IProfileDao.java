package com.gym.dao;

import java.util.Optional;

public interface IProfileDao<T, ID> extends ICreateAndReadDao<T, ID> {
    void update(T entity);

    Optional<T> findByUsername(String username);
}
