package com.gym.service;

public interface UpdatableService<T, ID> extends GeneralService<T, ID>{
    void update(T entity);
}
