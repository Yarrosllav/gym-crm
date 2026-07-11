package com.gym.dao;

public interface DeletableDAO<T, ID> extends UpdatableDAO<T, ID>{
    void delete(ID id);
}
