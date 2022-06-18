package com.dtuchs.libs.jdbc.base;

import javax.sql.DataSource;

public class DSBuilder {

    private final ConnConfig connConfig = new ConnConfig();
    
    public DSBuilder postgres() {
        connConfig.jdbcClass = "org.postgresql.Driver";
        connConfig.jdbcPrefix = "jdbc:postgresql";
        return this;
    }

    public DSBuilder mySql() {
        connConfig.jdbcClass = "com.mysql.jdbc.Driver";
        connConfig.jdbcPrefix = "jdbc:mysql";
        return this;
    }

    public DSBuilder h2() {
        connConfig.jdbcClass = "org.h2.Driver";
        connConfig.jdbcPrefix = "jdbc:h2";
        return this;
    }

    public DSBuilder vertica() {
        connConfig.jdbcClass = "com.vertica.jdbc.Driver";
        connConfig.jdbcPrefix = "jdbc:vertica";
        return this;
    }

    public DSBuilder withJdbcUrl(String jdbcUrl) {
        connConfig.jdbcUrl = jdbcUrl;
        return this;
    }

    public DSBuilder withDbHost(String dbHost) {
        connConfig.dbHost = dbHost;
        return this;
    }

    public DSBuilder withDbName(String dbName) {
        connConfig.dbName = dbName;
        return this;
    }

    public DSBuilder withUsername(String username) {
        connConfig.username = username;
        return this;
    }

    public DSBuilder withPassword(String password) {
        connConfig.password = password;
        return this;
    }

    public DSBuilder withDbPort(int dbPort) {
        connConfig.dbPort = dbPort;
        return this;
    }

    /**
     * Build DataSource
     */
    public DataSource build() {
        return DataSourceContext.INSTANCE.get(connConfig.validate());
    }
}
