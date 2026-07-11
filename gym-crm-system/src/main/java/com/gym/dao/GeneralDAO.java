package com.gym.dao;

import java.util.Collection;

public interface GeneralDAO<T, ID> {
    T findById(ID id);
    void create(T entity);
    Collection<T> findAll();
}
