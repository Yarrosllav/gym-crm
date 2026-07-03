package com.gym.dao;

public interface UpdatableDAO<T, ID> extends GeneralDAO<T, ID>{
    void update(T entity);
}
