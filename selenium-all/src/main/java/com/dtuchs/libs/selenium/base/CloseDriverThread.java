package com.dtuchs.libs.selenium.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import javax.annotation.Nonnull;
import java.util.Queue;

class CloseDriverThread extends Thread {

    private final Queue<Thread> allWebDriverThreads;

    CloseDriverThread(@Nonnull Queue<Thread> allWebDriverThreads) {
        this.allWebDriverThreads = allWebDriverThreads;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            closeUnusedWebdrivers();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void closeUnusedWebdrivers() {
        for (Thread thread : allWebDriverThreads) {
            if (!thread.isAlive()) {
                closeWebDriver(thread);
            }
        }
    }

    private void closeWebDriver(@Nonnull Thread thread) {
        allWebDriverThreads.remove(thread);
        WebDriver driver = WebDriverContainer.INSTANCE
                .getWebDriver(thread);

        if (driver != null) {
            try {
                driver.quit();
            } catch (WebDriverException e) {
                // do nothing, driver already closed
            }
        }
    }
}

