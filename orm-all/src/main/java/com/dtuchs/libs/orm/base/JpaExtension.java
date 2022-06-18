package com.dtuchs.libs.orm.base;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;

import static java.lang.System.currentTimeMillis;

public class JpaExtension implements BeforeAllCallback {

    private static final Logger log = LoggerFactory.getLogger(JpaExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent(JpaExtensionCallback.class);
    }

    static class JpaExtensionCallback implements ExtensionContext.Store.CloseableResource {
        public JpaExtensionCallback() {
        }

        @Override
        public void close() {
            for (EntityManagerFactory emf : EmfContext.INSTANCE.storedEmf()) {
                if (emf != null && emf.isOpen()) {
                    long start = currentTimeMillis();
                    emf.close();
                }
            }
        }
    }
}