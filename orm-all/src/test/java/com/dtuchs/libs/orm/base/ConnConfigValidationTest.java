package com.dtuchs.libs.orm.base;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConnConfigValidationTest {

    static final String CFG_FIELD_NAME = "connConfig";

    @Test
    void dbUsernameValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new EmfBuilder()
                        .h2()
                        .jdbcUrl("jdbc:h2:mem:testdb")
                        .password("")
                        .persistenceUnitName("test")
                        .build());

        assertEquals("db username must not be null.", exception.getMessage());
    }

    @Test
    void dbPasswordValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new EmfBuilder()
                .h2()
                .jdbcUrl("jdbc:h2:mem:testdb")
                .username("")
                .persistenceUnitName("test")
                .build());

        assertEquals("db password must not be null.", exception.getMessage());
    }

    @Test
    void persistenceUnitNameValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new EmfBuilder()
                .h2()
                .jdbcUrl("jdbc:h2:mem:testdb")
                .username("")
                .password("")
                .build());

        assertEquals("persistence unit name must not be null.", exception.getMessage());
    }

    @Test
    void jdbcClassValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  {
            EmfBuilder builder = new EmfBuilder();
            Field connConfig = EmfBuilder.class.getDeclaredField(CFG_FIELD_NAME);
            connConfig.setAccessible(true);
            ConnConfig testedCfg = new ConnConfig();
            testedCfg.jdbcPrefix = "jdbc:h2";
            testedCfg.dialect = "org.hibernate.dialect.H2Dialect";
            connConfig.set(builder, testedCfg);

            builder.jdbcUrl("jdbc:h2:mem:testdb")
                    .username("")
                    .password("")
                    .persistenceUnitName("test")
                    .build();
        });
        assertEquals("JDBC class name must not be null.", exception.getMessage());
    }

    @Test
    void dialectValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  {
            EmfBuilder builder = new EmfBuilder();
            Field connConfig = EmfBuilder.class.getDeclaredField(CFG_FIELD_NAME);
            connConfig.setAccessible(true);
            ConnConfig testedCfg = new ConnConfig();
            testedCfg.jdbcClass = "org.h2.jdbcx.JdbcDataSource";
            testedCfg.jdbcPrefix = "jdbc:h2";
            connConfig.set(builder, testedCfg);

            builder.jdbcUrl("jdbc:h2:mem:testdb")
                    .username("")
                    .password("")
                    .persistenceUnitName("test")
                    .build();
        });
        assertEquals("Hibernate dialect must not be null.", exception.getMessage());
    }

    @Test
    void jdbcHostValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  new EmfBuilder()
                .h2()
                .dbName("testdb")
                .username("")
                .password("")
                .persistenceUnitName("test")
                .build());

        assertEquals("db host must not be null.", exception.getMessage());
    }

    @Test
    void dbNameValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  new EmfBuilder()
                .h2()
                .dbHost("mem")
                .username("")
                .password("")
                .persistenceUnitName("test")
                .build());

        assertEquals("db name must not be null.", exception.getMessage());
    }

    @Test
    void jdbcPortValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  new EmfBuilder()
                .h2()
                .dbHost("mem")
                .dbName("testdb")
                .username("")
                .password("")
                .persistenceUnitName("test")
                .build());

        assertEquals("db port must not be null.", exception.getMessage());
    }

    @Test
    void jdbcPrefixValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->  {
            EmfBuilder builder = new EmfBuilder();
            Field connConfig = EmfBuilder.class.getDeclaredField(CFG_FIELD_NAME);
            connConfig.setAccessible(true);
            ConnConfig testedCfg = new ConnConfig();
            testedCfg.jdbcClass = "org.h2.jdbcx.JdbcDataSource";
            testedCfg.dialect = "org.hibernate.dialect.H2Dialect";
            connConfig.set(builder, testedCfg);

            builder.dbHost("mem")
                    .dbName("testdb")
                    .dbPort(1234)
                    .username("")
                    .password("")
                    .persistenceUnitName("test")
                    .build();
        });
        assertEquals("JDBC prefix must not be null. For example: jdbc:postgresql or jdbc:mysql", exception.getMessage());
    }
}
