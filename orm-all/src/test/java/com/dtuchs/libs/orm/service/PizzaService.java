package com.dtuchs.libs.orm.service;

import com.dtuchs.libs.orm.base.EmfBuilder;
import com.dtuchs.libs.orm.base.JpaService;

import javax.persistence.EntityManager;

public class PizzaService extends JpaService {

    public PizzaService(EntityManager em) {
        super(em);
    }

    public PizzaService() {
        super(new EmfBuilder()
                .h2()
                .jdbcUrl("jdbc:h2:mem:EmfBuilderTest")
                .username("")
                .password("")
                .persistenceUnitName("test")
                .build()
                .createEntityManager());
    }

    public Pizza get(int id) {
        return em.find(Pizza.class, id);
    }

    public void save(Pizza pizza) {
        persist(pizza);
    }

    public Pizza getPizzaByName(String name) {
        return em.createQuery(
                        "select a from Pizza a where a.name=:name",
                        Pizza.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    public void updatePizzaName(String oldName, String newName) {
        Pizza p = getPizzaByName(oldName);
        p.setName(newName);
        merge(p);
    }
}