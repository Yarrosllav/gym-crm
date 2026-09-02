package com.gym.service;

import com.gym.dao.ICreateAndReadDao;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class AbstractService<T, ID extends Serializable> {

    protected final ICreateAndReadDao<T, ID> dao;

    protected AbstractService(ICreateAndReadDao<T, ID> dao) {
        this.dao = dao;
    }

    @Transactional
    public void create(T entity) {
        dao.create(entity);
    }

    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        return dao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return dao.findAll();
    }
}
