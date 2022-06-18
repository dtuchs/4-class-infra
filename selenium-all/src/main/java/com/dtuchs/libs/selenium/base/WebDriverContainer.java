package com.dtuchs.libs.selenium.base;

import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.currentThread;

public enum WebDriverContainer {
    INSTANCE;

    private final Map<Thread, WebDriver> webDriverHolder = new ConcurrentHashMap<>();
    private final Queue<Thread> allDriverThreads = new ConcurrentLinkedQueue<>();
    private volatile boolean cleanupThreadStarted = false;

    /**
     * @throws IllegalStateException if webdriver not bound to current thread
     */
    @Nonnull
    public WebDriver getRequiredWebDriver() {
        final Thread th = currentThread();
        return Optional.ofNullable(webDriverHolder.get(th))
                .orElseThrow(() -> new IllegalStateException("No webdriver found for thread " + th.getId()));
    }

    @Nonnull
    public WebDriver getOrInitWebDriver() {
        final Thread th = currentThread();
        if (!webDriverHolder.containsKey(th)) {
            WebDriver driver = WebDriverFactory.createWebDriver();
            webDriverHolder.put(th, driver);
            markForAutoClose(th);
        }
        return getRequiredWebDriver();
    }

    public void closeWebDriver() {
        final Thread th = currentThread();
        WebDriver webDriver = getWebDriver(th);
        if (webDriver != null) {
            webDriver.quit();
        }
        webDriverHolder.remove(th);
    }

    @Nullable
    public WebDriver getWebDriver(Thread thread) {
        return webDriverHolder.get(thread);
    }

    private void markForAutoClose(@Nonnull Thread thread) {
        allDriverThreads.add(thread);
        if (!cleanupThreadStarted) {
            synchronized (this) {
                if (!cleanupThreadStarted) {
                    new CloseDriverThread(allDriverThreads).start();
                    cleanupThreadStarted = true;
                }
            }
        }
    }
}
