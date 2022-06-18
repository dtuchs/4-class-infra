package com.dtuchs.libs.orm.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

enum EmfContext {
    INSTANCE;

    private final Logger log = LoggerFactory.getLogger(EmfContext.class);
    private final Map<ConnConfig, EntityManagerFactory> container = new HashMap<>();

    /**
     * Get existing or compute a new EntityManagerFactory for given configuration.
     */
    synchronized EntityManagerFactory get(ConnConfig connConfig) {
        if (container.containsKey(connConfig))
            return container.get(connConfig);
        else {
            log.warn("### Init EntityManagerFactory ###");

            Map<String, String> settings = new HashMap<>();
            settings.put(
                    "hibernate.connection.provider_class",
                    "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
            );
            settings.put("hibernate.hikari.dataSourceClassName", connConfig.jdbcClass);
            settings.put("hibernate.hikari.maximumPoolSize", "32");
            settings.put("hibernate.hikari.minimumIdle", "0");
            settings.put("hibernate.hikari.idleTimeout", "240000");
            settings.put("hibernate.hikari.maxLifetime", "270000");
            settings.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
            settings.put("hibernate.hikari.dataSource.url", Objects.requireNonNullElseGet(connConfig.jdbcUrl, () -> connConfig.jdbcPrefix + "://" + connConfig.dbHost + ":" + connConfig.dbPort + "/" + connConfig.dbName));
            settings.put("hibernate.hikari.dataSource.user", connConfig.username);
            settings.put("hibernate.hikari.dataSource.password", connConfig.password);
            settings.put("hibernate.dialect", connConfig.dialect);

            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(connConfig.persistenceUnitName, settings);
            container.put(connConfig, entityManagerFactory);
            return entityManagerFactory;
        }
    }

    /**
     * Get all of stored EntityManagerFactories.
     */
    Collection<EntityManagerFactory> storedEmf() {
        return container.values();
    }
}
