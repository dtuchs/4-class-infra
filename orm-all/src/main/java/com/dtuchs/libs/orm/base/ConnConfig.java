package com.dtuchs.libs.orm.base;

import java.util.Objects;

final class ConnConfig {

    String dbHost;
    Integer dbPort;
    String dbName;

    String username;
    String password;
    String persistenceUnitName;

    String jdbcClass;
    String jdbcPrefix;
    String dialect;

    String jdbcUrl;

    ConnConfig validate() {
        if (jdbcUrl == null) {
            if (dbHost == null)
                throw new IllegalStateException("db host must not be null.");
            if (dbName == null)
                throw new IllegalStateException("db name must not be null.");
            if (dbPort == null)
                throw new IllegalStateException("db port must not be null.");
            if (jdbcPrefix == null)
                throw new IllegalStateException("JDBC prefix must not be null. For example: jdbc:postgresql or jdbc:mysql");
        }

        if (username == null)
            throw new IllegalStateException("db username must not be null.");
        if (password == null)
            throw new IllegalStateException("db password must not be null.");
        if (persistenceUnitName == null)
            throw new IllegalStateException("persistence unit name must not be null.");
        if (jdbcClass == null)
            throw new IllegalStateException("JDBC class name must not be null.");
        if (dialect == null)
            throw new IllegalStateException("Hibernate dialect must not be null.");
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnConfig that = (ConnConfig) o;
        return Objects.equals(dbHost, that.dbHost) && Objects.equals(dbName, that.dbName) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(persistenceUnitName, that.persistenceUnitName) && Objects.equals(dbPort, that.dbPort) && Objects.equals(jdbcClass, that.jdbcClass) && Objects.equals(jdbcPrefix, that.jdbcPrefix) && Objects.equals(dialect, that.dialect) && Objects.equals(jdbcUrl, that.jdbcUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbHost, dbName, username, password, persistenceUnitName, dbPort, jdbcClass, jdbcPrefix, dialect, jdbcUrl);
    }
}
