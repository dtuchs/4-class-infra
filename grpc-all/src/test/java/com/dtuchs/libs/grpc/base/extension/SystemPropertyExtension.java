package com.dtuchs.libs.grpc.base.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SystemPropertyExtension implements AfterEachCallback, BeforeEachCallback {

    @Override
    public void afterEach(ExtensionContext context)  {
        SystemProperty annotation = context.getRequiredTestMethod().getAnnotation(SystemProperty.class);
        System.clearProperty(annotation.key());
    }

    @Override
    public void beforeEach(ExtensionContext context)  {
        SystemProperty annotation = context.getRequiredTestMethod().getAnnotation(SystemProperty.class);
        System.setProperty(annotation.key(), annotation.value());
    }
}
