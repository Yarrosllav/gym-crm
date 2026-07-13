package com.gym.dao;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractDao<T, ID extends Serializable> {

    protected final SessionFactory sessionFactory;

    private final Class<T> entityClass;

    protected AbstractDao(SessionFactory sessionFactory, Class<T> entityClass) {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    protected Session session() {
        return sessionFactory.getCurrentSession();
    }

    public void create(T entity) {
        log.debug("Persisting {} entity: {}", entityClass.getSimpleName(), entity);
        session().persist(entity);
    }

    public void update(T entity) {
        log.debug("Merging {} entity: {}", entityClass.getSimpleName(), entity);
        session().merge(entity);
    }

    public void delete(T entity) {
        log.debug("Removing {} entity: {}", entityClass.getSimpleName(), entity);
        session().remove(entity);
    }

    public Optional<T> findById(ID id) {
        log.debug("Fetching {} by id: {}", entityClass.getSimpleName(), id);
        return Optional.ofNullable(session().get(entityClass, id));
    }

    public List<T> findAll() {
        log.debug("Fetching all {} entities", entityClass.getSimpleName());
        return session()
                .createQuery("from " + entityClass.getSimpleName(), entityClass)
                .list();
    }
}
