package com.dtuchs.libs.orm.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.zapodot.junit.db.annotations.EmbeddedDatabase;
import org.zapodot.junit.db.annotations.EmbeddedDatabaseTest;
import org.zapodot.junit.db.common.Engine;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(JpaExtension.class)
@EmbeddedDatabaseTest(
        engine = Engine.H2,
        initialSqls = "CREATE TABLE ADVERTISER(id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR(512)); "
                + "INSERT INTO ADVERTISER(id, name) VALUES (1, 'Max Maze')"
)
class EmfBuilderTest {

    @EmbeddedDatabase
    private Connection connection;

    EntityManagerFactory emf = new EmfBuilder()
            .h2()
            .withJdbcUrl("jdbc:h2:mem:EmfBuilderTest")
            .withUsername("")
            .withPassword("")
            .withPersistenceUnitName("test")
            .build();

    @AfterEach
    void dropTable() throws SQLException {
            connection.createStatement().executeUpdate("DROP TABLE ADVERTISER");
            connection.close();
    }

    @Test
    void emfBuilderTest() {
        AdvertiserManager am = new AdvertiserManager(emf.createEntityManager());
        Advertiser advertiser = am.get(1);
        assertNotNull(advertiser);
        assertEquals("Max Maze", advertiser.getName());
    }

    @Test
    void persistEntityTest() {
        AdvertiserManager am = new AdvertiserManager(emf.createEntityManager());
        Advertiser advertiser = new Advertiser();
        advertiser.setName("Ivan Ivanov");
        am.persist(advertiser);
        Advertiser createdEntity = am.get(2);
        assertEquals("Ivan Ivanov", createdEntity.getName());
    }

    @Test
    void updateEntityTest() {
        AdvertiserManager am = new AdvertiserManager(emf.createEntityManager());
        Advertiser advertiser = am.get(1);
        advertiser.setName("Petr Petrov");
        am.update(advertiser);
        Advertiser updatedEntity = am.get(1);
        assertEquals("Petr Petrov", updatedEntity.getName());
    }

    @Test
    void refreshTest() {
        AdvertiserManager am = new AdvertiserManager(emf.createEntityManager());
        Advertiser advertiser = am.get(1);
        advertiser.setName("Fedor Fedorov");
        assertEquals("Fedor Fedorov", advertiser.getName());
        am.refresh(advertiser);
        assertEquals("Max Maze", advertiser.getName());
    }

    @Test
    void createTwoDifferentEmfTest() {
        EntityManagerFactory emf2 = new EmfBuilder()
                .h2()
                .withJdbcUrl("jdbc:h2:mem:testdb")
                .withUsername("")
                .withPassword("")
                .withPersistenceUnitName("test")
                .build();
        assertNotSame(emf, emf2);
    }

    @Test
    void threadLocalEmfBuilderTest() {
        EmfThreadLocal emfThreadLocal = buildDefaultEmfThreadLocal();
        AdvertiserManager am = new AdvertiserManager(emfThreadLocal.getEntityManager());
        Advertiser advertiser = am.get(1);
        assertNotNull(advertiser);
        assertEquals("Max Maze", advertiser.getName());
    }

    @Test
    void threadLocalGetEntityMagagerInSameThreadTest() {
        EmfThreadLocal emfThreadLocal = buildDefaultEmfThreadLocal();
        EntityManager em1 = emfThreadLocal.getEntityManager();
        EntityManager em2 = emfThreadLocal.getEntityManager();
        assertSame(em1, em2);
    }

    @Test
    void threadLocalGetEntityManagerInDifferentThreadsTest() throws InterruptedException {
        AtomicBoolean success = new AtomicBoolean(false);
        EmfThreadLocal emfThreadLocal = buildDefaultEmfThreadLocal();
        EntityManager em1 = emfThreadLocal.getEntityManager();

        Thread otherThread = new Thread(() ->  {
                EmfThreadLocal emfThreadLocal2 = buildDefaultEmfThreadLocal();
                EntityManager em2 = emfThreadLocal2.getEntityManager();
                if(em1 != em2) {
                    success.set(true);
                }
                em2.close();
        });
        otherThread.start();
        otherThread.join();
        assertTrue(success.get(), "Entity managers in different local threads should be different");
        assertTrue(em1.isOpen(), "Closed entity manager in other thread should not affect current entity manager");
    }

    private EmfThreadLocal buildDefaultEmfThreadLocal() {
        return new EmfBuilder()
                .h2()
                .withJdbcUrl("jdbc:h2:mem:EmfBuilderTest")
                .withUsername("")
                .withPassword("")
                .withPersistenceUnitName("test")
                .buildThreadLocal();
    }

    static class AdvertiserManager extends JpaManager {
        public AdvertiserManager(EntityManager em) {
            super(em);
        }

        public Advertiser get(int id) {
            return em.find(Advertiser.class, id);
        }
    }
}