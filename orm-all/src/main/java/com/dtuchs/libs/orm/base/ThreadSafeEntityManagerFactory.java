package com.dtuchs.libs.orm.base;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

import static javax.persistence.SynchronizationType.SYNCHRONIZED;

public class ThreadSafeEntityManagerFactory implements EntityManagerFactory {

    private final EntityManagerFactory delegate;
    private final ThreadLocal<EntityManager> emThreadLocal;

    ThreadSafeEntityManagerFactory(EntityManagerFactory delegate) {
        this.delegate = delegate;
        this.emThreadLocal =
                ThreadLocal.withInitial(delegate::createEntityManager);
    }

    public EntityManagerFactory getDelegate() {
        return delegate;
    }

    @Override
    public EntityManager createEntityManager() {
        return emThreadLocal.get();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        final EntityManager em = emThreadLocal.get();
        if (!isCurrentEmContainsAllProperties(map)) {
            emThreadLocal.set(delegate.createEntityManager(map));
        }
        return emThreadLocal.get();
    }

    /**
     * Only default SynchronizationType SYNCHRONIZED supported for RESOURCE_LOCAL transaction type;
     * IllegalStateException will be thrown for SynchronizationType.UNSYNCHRONIZED
     */
    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        if (synchronizationType == SYNCHRONIZED) {
            return this.createEntityManager();
        } else return delegate.createEntityManager(synchronizationType);
    }

    /**
     * Only default SynchronizationType SYNCHRONIZED supported for RESOURCE_LOCAL transaction type;
     * IllegalStateException will be thrown for SynchronizationType.UNSYNCHRONIZED
     */
    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        if (synchronizationType == SYNCHRONIZED) {
            return this.createEntityManager(map);
        } else return delegate.createEntityManager(synchronizationType, map);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(String s, Query query) {
        delegate.addNamedQuery(s, query);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return delegate.unwrap(aClass);
    }

    @Override
    public <T> void addNamedEntityGraph(String s, EntityGraph<T> entityGraph) {
        delegate.addNamedEntityGraph(s, entityGraph);
    }

    private boolean isCurrentEmContainsAllProperties(Map map) {
        final EntityManager em = emThreadLocal.get();
        return em.getProperties().keySet().containsAll(map.keySet()) &&
                em.getProperties().entrySet().stream()
                        .filter(map::containsKey)
                        .allMatch(e -> e.getValue().equals(map.get(e.getKey())));
    }
}
