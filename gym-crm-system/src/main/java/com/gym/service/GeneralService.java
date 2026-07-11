package com.gym.service;

public interface GeneralService<T, ID> {
    void create(T entity);
    T findById(ID id);
}
