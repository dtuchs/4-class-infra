package com.dtuchs.libs.jdbc.base;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zapodot.junit.db.EmbeddedDatabaseExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DSBuilderTest {

    @RegisterExtension
    static EmbeddedDatabaseExtension edf = EmbeddedDatabaseExtension.Builder.h2()
            .withName("testdb")
            .withInitialSql("CREATE TABLE ADVERTISER(id INTEGER PRIMARY KEY, name VARCHAR(512)); "
                    + "INSERT INTO ADVERTISER(id, name) VALUES (1, 'Max Maze')")
            .build();

    @Test
    void dsBuilderTest() {
        final int testId = 2;
        final String testName = "Skeeper";

        DataSource dataSource = new DSBuilder()
                .h2()
                .withJdbcUrl("jdbc:h2:mem:testdb")
                .withUsername("")
                .withPassword("")
                .build();

        JdbcTemplate template = new JdbcTemplate(dataSource);
        final int count = template.update("INSERT INTO ADVERTISER(id, name) VALUES(?,?)",
                testId,
                testName
        );

        assertEquals(1, count);
        assertEquals(testName, template.queryForObject("SELECT name from ADVERTISER where id = ?", String.class, testId));
    }
}