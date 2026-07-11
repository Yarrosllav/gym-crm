package com.gym.service;

public interface DeletableService<T, ID> extends UpdatableService<T, ID>{
    void delete(ID id);
}
