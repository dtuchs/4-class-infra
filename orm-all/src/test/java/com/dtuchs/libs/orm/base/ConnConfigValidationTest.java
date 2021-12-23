package com.dtuchs.libs.orm.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnConfigValidationTest {

    @Test
    void dbUsernameValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new EmfBuilder()
                        .h2()
                        .withJdbcUrl("jdbc:h2:mem:testdb")
                        .withPassword("")
                        .withPersistenceUnitName("test")
                        .build());

        assertEquals("db username must not be null.", exception.getMessage());
    }

    @Test
    void dbPasswordValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new EmfBuilder()
                .h2()
                .withJdbcUrl("jdbc:h2:mem:testdb")
                .withUsername("")
                .withPersistenceUnitName("test")
                .build());

        assertEquals("db password must not be null.", exception.getMessage());
    }

    @Test
    void persistenceUnitNameValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new EmfBuilder()
                .h2()
                .withJdbcUrl("jdbc:h2:mem:testdb")
                .withUsername("")
                .withPassword("")
                .build());

        assertEquals("persistence unit name must not be null.", exception.getMessage());
    }

    @Test
    void jdbcClassValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  {
            EmfBuilder builder = new EmfBuilder();
            builder.config().jdbcPrefix = "jdbc:h2";
            builder.config().dialect = "org.hibernate.dialect.H2Dialect";

            builder.withJdbcUrl("jdbc:h2:mem:testdb")
                    .withUsername("")
                    .withPassword("")
                    .withPersistenceUnitName("test")
                    .build();
        });
        assertEquals("JDBC class name must not be null.", exception.getMessage());
    }

    @Test
    void dialectValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  {
            EmfBuilder builder = new EmfBuilder();
            builder.config().jdbcClass = "org.h2.jdbcx.JdbcDataSource";
            builder.config().jdbcPrefix = "jdbc:h2";

            builder.withJdbcUrl("jdbc:h2:mem:testdb")
                    .withUsername("")
                    .withPassword("")
                    .withPersistenceUnitName("test")
                    .build();
        });
        assertEquals("Hibernate dialect must not be null.", exception.getMessage());
    }

    @Test
    void jdbcHostValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  new EmfBuilder()
                .h2()
                .withDbName("testdb")
                .withUsername("")
                .withPassword("")
                .withPersistenceUnitName("test")
                .build());

        assertEquals("db host must not be null.", exception.getMessage());
    }

    @Test
    void dbNameValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  new EmfBuilder()
                .h2()
                .withDbHost("mem")
                .withUsername("")
                .withPassword("")
                .withPersistenceUnitName("test")
                .build());

        assertEquals("db name must not be null.", exception.getMessage());
    }

    @Test
    void jdbcPortValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  new EmfBuilder()
                .h2()
                .withDbHost("mem")
                .withDbName("testdb")
                .withUsername("")
                .withPassword("")
                .withPersistenceUnitName("test")
                .build());

        assertEquals("db port must not be null.", exception.getMessage());
    }

    @Test
    void jdbcPrefixValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  {
            EmfBuilder builder = new EmfBuilder();
            builder.config().jdbcClass = "org.h2.jdbcx.JdbcDataSource";
            builder.config().dialect = "org.hibernate.dialect.H2Dialect";

            builder.withDbHost("mem")
                    .withDbName("testdb")
                    .withDbPort(1234)
                    .withUsername("")
                    .withPassword("")
                    .withPersistenceUnitName("test")
                    .build();
        });
        assertEquals("JDBC prefix must not be null. For example: jdbc:postgresql or jdbc:mysql", exception.getMessage());
    }
}
