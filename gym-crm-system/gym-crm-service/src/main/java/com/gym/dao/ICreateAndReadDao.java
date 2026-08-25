package com.gym.dao;

public interface ICreateAndReadDao<T, ID> extends IReadOnlyDao<T, ID> {
    void create(T entity);
}
