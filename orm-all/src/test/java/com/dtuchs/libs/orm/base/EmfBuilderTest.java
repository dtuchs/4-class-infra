package com.dtuchs.libs.orm.base;

import com.dtuchs.libs.orm.service.Pizza;
import com.dtuchs.libs.orm.service.PizzaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.zapodot.junit.db.annotations.EmbeddedDatabase;
import org.zapodot.junit.db.annotations.EmbeddedDatabaseTest;
import org.zapodot.junit.db.common.Engine;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(JpaExtension.class)
@EmbeddedDatabaseTest(
        engine = Engine.H2,
        initialSqls = "CREATE TABLE PIZZA(id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR(512)); "
                + "INSERT INTO PIZZA(id, name) VALUES (1, 'Pepperoni')"
)
class EmfBuilderTest {

    @EmbeddedDatabase
    private Connection connection;

    EntityManagerFactory emf = new EmfBuilder()
            .h2()
            .jdbcUrl("jdbc:h2:mem:EmfBuilderTest")
            .username("")
            .password("")
            .persistenceUnitName("test")
            .build();

    @AfterEach
    void dropTable() throws SQLException {
        connection.createStatement().executeUpdate("DROP TABLE PIZZA");
        connection.close();
    }

    @Test
    void emfBuilderTest() {
        PizzaService am = new PizzaService(emf.createEntityManager());
        Pizza pizza = am.get(1);
        assertNotNull(pizza);
        assertEquals("Pepperoni", pizza.getName());
    }

    @Test
    void persistEntityTest() {
        PizzaService am = new PizzaService(emf.createEntityManager());
        Pizza pizza = new Pizza();
        pizza.setName("Chicken bomboni");
        am.save(pizza);
        Pizza createdEntity = am.get(2);
        assertEquals("Chicken bomboni", createdEntity.getName());
    }

    @Test
    void updateEntityTest() {
        PizzaService am = new PizzaService(emf.createEntityManager());
        Pizza pizza = am.get(1);
        pizza.setName("4 cheeses");
        am.merge(pizza);
        Pizza updatedEntity = am.get(1);
        assertEquals("4 cheeses", updatedEntity.getName());
    }

    @Test
    void refreshTest() {
        PizzaService am = new PizzaService(emf.createEntityManager());
        Pizza pizza = am.get(1);
        pizza.setName("4 cheeses");
        assertEquals("4 cheeses", pizza.getName());
        am.refresh(pizza);
        assertEquals("Pepperoni", pizza.getName());
    }

    @Test
    void createTwoDifferentEmfTest() {
        EntityManagerFactory emf2 = new EmfBuilder()
                .h2()
                .jdbcUrl("jdbc:h2:mem:testdb")
                .username("")
                .password("")
                .persistenceUnitName("test")
                .build();
        assertNotSame(emf, emf2);
    }

    @Test
    void threadLocalEmfBuilderTest() {
        EntityManagerFactory emfThreadLocal = buildDefaultEmfThreadLocal();
        PizzaService am = new PizzaService(emfThreadLocal.createEntityManager());
        Pizza pizza = am.get(1);
        assertNotNull(pizza);
        assertEquals("Pepperoni", pizza.getName());
    }

    @Test
    void threadLocalGetEntityMagagerInSameThreadTest() {
        EntityManagerFactory emfThreadLocal = buildDefaultEmfThreadLocal();
        EntityManager em1 = emfThreadLocal.createEntityManager();
        EntityManager em2 = emfThreadLocal.createEntityManager();
        assertSame(em1, em2);
    }

    @Test
    void threadLocalGetEntityMagagerMapInSameThreadTest() {
        EntityManagerFactory emfThreadLocal = buildDefaultEmfThreadLocal();
        EntityManager em1 = emfThreadLocal.createEntityManager(Map.of("key0", "value0"));
        EntityManager em2 = emfThreadLocal.createEntityManager(Map.of("key0", "value0"));
        assertSame(em1, em2);
    }

    @Test
    void threadLocalGetEntityMagagersInSameThreadTestWithDifferentParams() {
        EntityManagerFactory emfThreadLocal = buildDefaultEmfThreadLocal();
        EntityManager em1 = emfThreadLocal.createEntityManager();
        EntityManager em2 = emfThreadLocal.createEntityManager(Map.of("key0", "value0"));
        assertNotSame(em1, em2);
    }

    @Test
    void threadLocalGetEntityMagagerWithUNSYNCHRONIZEDSynchronizationTypeTest() {
        EntityManagerFactory emfThreadLocal = buildDefaultEmfThreadLocal();
        assertThrows(IllegalStateException.class, () -> {
            EntityManager em = emfThreadLocal.createEntityManager(SynchronizationType.UNSYNCHRONIZED);
        });
    }

    @Test
    void threadLocalcreateEntityManagerInDifferentThreadsTest() throws InterruptedException {
        AtomicBoolean success = new AtomicBoolean(false);
        EntityManagerFactory emfThreadLocal = buildDefaultEmfThreadLocal();
        EntityManager em1 = emfThreadLocal.createEntityManager();

        Thread otherThread = new Thread(() -> {
            EntityManagerFactory emfThreadLocal2 = buildDefaultEmfThreadLocal();
            EntityManager em2 = emfThreadLocal2.createEntityManager();
            if (em1 != em2) {
                success.set(true);
            }
            em2.close();
        });
        otherThread.start();
        otherThread.join();
        assertTrue(success.get(), "Entity managers in different local threads should be different");
        assertTrue(em1.isOpen(), "Closed entity manager in other thread should not affect current entity manager");
    }

    @Test
    void threadLocalcreateEntityManagerMapInDifferentThreadsTest() throws InterruptedException {
        AtomicBoolean success = new AtomicBoolean(false);
        EntityManagerFactory emfThreadLocal = buildDefaultEmfThreadLocal();
        EntityManager em1 = emfThreadLocal.createEntityManager(Map.of("key0", "value0"));

        Thread otherThread = new Thread(() -> {
            EntityManagerFactory emfThreadLocal2 = buildDefaultEmfThreadLocal();
            EntityManager em2 = emfThreadLocal2.createEntityManager(Map.of("key0", "value0"));
            if (em1 != em2) {
                success.set(true);
            }
            em2.close();
        });
        otherThread.start();
        otherThread.join();
        assertTrue(success.get(), "Entity managers in different local threads should be different");
        assertTrue(em1.isOpen(), "Closed entity manager in other thread should not affect current entity manager");
    }

    private EntityManagerFactory buildDefaultEmfThreadLocal() {
        return new EmfBuilder().h2()
                .jdbcUrl("jdbc:h2:mem:EmfBuilderTest")
                .username("")
                .password("")
                .persistenceUnitName("test")
                .build();
    }
}