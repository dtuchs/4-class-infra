package com.dtuchs.libs.orm.base;

import javax.persistence.EntityManagerFactory;

public class EmfBuilder {

    private ConnConfig connConfig;

    ConnConfig config() {
        if (connConfig == null)
            connConfig = new ConnConfig();
        return connConfig;
    }

    public EmfBuilder postgres() {
        config().jdbcClass = "org.postgresql.ds.PGSimpleDataSource";
        config().jdbcPrefix = "jdbc:postgresql";
        config().dialect = "org.hibernate.dialect.PostgreSQL94Dialect";
        return this;
    }

    public EmfBuilder mySql() {
        config().jdbcClass = "com.mysql.cj.jdbc.MysqlDataSource";
        config().jdbcPrefix = "jdbc:mysql";
        config().dialect = "org.hibernate.dialect.MySQL8Dialect";
        return this;
    }

    public EmfBuilder h2() {
        config().jdbcClass = "org.h2.jdbcx.JdbcDataSource";
        config().jdbcPrefix = "jdbc:h2";
        config().dialect = "org.hibernate.dialect.H2Dialect";
        return this;
    }

    public EmfBuilder withJdbcUrl(String jdbcUrl) {
        config().jdbcUrl = jdbcUrl;
        return this;
    }

    public EmfBuilder withDbHost(String dbHost) {
        config().dbHost = dbHost;
        return this;
    }

    public EmfBuilder withDbName(String dbName) {
        config().dbName = dbName;
        return this;
    }

    public EmfBuilder withUsername(String username) {
        config().username = username;
        return this;
    }

    public EmfBuilder withPassword(String password) {
        config().password = password;
        return this;
    }

    public EmfBuilder withPersistenceUnitName(String persistenceUnitName) {
        config().persistenceUnitName = persistenceUnitName;
        return this;
    }

    public EmfBuilder withDbPort(int dbPort) {
        config().dbPort = dbPort;
        return this;
    }

    public EmfBuilder withHibernateDialect(String dialect) {
        config().dialect = dialect;
        return this;
    }

    /**
     * Build EntityManagerFactory
     */
    public EntityManagerFactory build() {
        return EmfContext.INSTANCE.get(config().validate());
    }

    /**
     * Build and wrap an instance of EntityManagerFactory to proxy object
     */
    public EmfThreadLocal buildThreadLocal() {
        return new EmfThreadLocal(EmfContext.INSTANCE.get(config().validate()));
    }
}
