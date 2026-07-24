package com.gym.dao;

import java.util.List;
import java.util.Optional;

public interface IReadOnlyDao<T, ID> {
    Optional<T> findById(ID id);

    List<T> findAll();
}
