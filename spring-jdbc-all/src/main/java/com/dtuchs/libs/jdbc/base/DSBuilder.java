package com.dtuchs.libs.jdbc.base;

import javax.sql.DataSource;

public class DSBuilder {

    private ConnConfig connConfig;

    ConnConfig config() {
        if (connConfig == null)
            connConfig = new ConnConfig();
        return connConfig;
    }

    public DSBuilder postgres() {
        config().jdbcClass = "org.postgresql.Driver";
        config().jdbcPrefix = "jdbc:postgresql";
        return this;
    }

    public DSBuilder mySql() {
        config().jdbcClass = "com.mysql.jdbc.Driver";
        config().jdbcPrefix = "jdbc:mysql";
        return this;
    }

    public DSBuilder h2() {
        config().jdbcClass = "org.h2.Driver";
        config().jdbcPrefix = "jdbc:h2";
        return this;
    }

    public DSBuilder vertica() {
        config().jdbcClass = "com.vertica.jdbc.Driver";
        config().jdbcPrefix = "jdbc:vertica";
        return this;
    }

    public DSBuilder withJdbcUrl(String jdbcUrl) {
        config().jdbcUrl = jdbcUrl;
        return this;
    }

    public DSBuilder withDbHost(String dbHost) {
        config().dbHost = dbHost;
        return this;
    }

    public DSBuilder withDbName(String dbName) {
        config().dbName = dbName;
        return this;
    }

    public DSBuilder withUsername(String username) {
        config().username = username;
        return this;
    }

    public DSBuilder withPassword(String password) {
        config().password = password;
        return this;
    }

    public DSBuilder withDbPort(int dbPort) {
        config().dbPort = dbPort;
        return this;
    }

    /**
     * Build DataSource
     */
    public DataSource build() {
        return DataSourceContext.INSTANCE.get(config().validate());
    }
}
