package me.nuguri.resc.repository.impl;

import me.nuguri.resc.repository.BaseRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;

@Transactional
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private final EntityManager entityManager;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public T generate(T t) {
        entityManager.persist(t);
        return t;
    }

    @Override
    public T merge(T t) {
        entityManager.merge(t);
        return t;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean contains(T t) {
        return entityManager.contains(t);
    }

    public void remove(T t) {
        entityManager.remove(t);
    }

}
