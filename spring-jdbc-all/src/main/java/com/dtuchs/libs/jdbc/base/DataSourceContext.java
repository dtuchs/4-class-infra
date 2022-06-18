package com.dtuchs.libs.jdbc.base;

import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

enum DataSourceContext {
    INSTANCE;

    private final Logger log = LoggerFactory.getLogger(DataSourceContext.class);
    private final Map<ConnConfig, DataSource> container = new HashMap<>();

    /**
     * Get existing or compute a new DataSource for given configuration.
     */
    synchronized DataSource get(ConnConfig connConfig) {
        if (container.containsKey(connConfig))
            return container.get(connConfig);
        else {
            log.warn("### Init DataSource ###");
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(Objects.requireNonNullElseGet(connConfig.jdbcUrl, () -> connConfig.jdbcPrefix + "://" + connConfig.dbHost + ":" + connConfig.dbPort + "/" + connConfig.dbName));
            config.setUsername(connConfig.username);
            config.setPassword(connConfig.password);
            config.setDriverClassName(connConfig.jdbcClass);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(1);
            P6DataSource p6DataSource = new P6DataSource(new HikariDataSource(config));

            container.put(connConfig, p6DataSource);
            return p6DataSource;
        }
    }
}
