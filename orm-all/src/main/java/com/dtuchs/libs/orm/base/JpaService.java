package com.dtuchs.libs.orm.base;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.function.Consumer;

public abstract class JpaService {

    protected final EntityManager em;

    public JpaService(EntityManager em) {
        this.em = em;
    }

    public <T> void persist(T entity) {
        transaction(em -> em.persist(entity));
    }

    public <T> void merge(T entity) {
        transaction(em -> em.merge(entity));
    }

    public <T> void remove(T entity) {
        transaction(em -> em.remove(entity));
    }

    public <T> void refresh(T entity) {
        em.refresh(entity);
    }

    protected void transaction(Consumer<EntityManager> action) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }
}
