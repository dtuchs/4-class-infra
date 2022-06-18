package com.dtuchs.libs.selenium.base;

import com.dtuchs.libs.selenium.base.config.Config;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.List;

import static com.dtuchs.libs.selenium.base.Utils.sleep;

public enum SmartElementLocator {
    INSTANCE;

    @Nonnull
    public WebElement findElement(@Nonnull SearchContext ctx, @Nonnull By selector) {
        StopWatch stopWatch = StopWatch.createStarted();
        while (stopWatch.getTime() <= Config.INSTANCE.actionTimeout) {
            try {
                return ctx.findElement(selector);
            } catch (Exception e) {
                sleep(Config.INSTANCE.defaultIterationTimeout);
            }
        }
        return ctx.findElement(selector);
    }

    @Nonnull
    public List<WebElement> findElements(@Nonnull SearchContext ctx, @Nonnull By selector) {
        StopWatch stopWatch = StopWatch.createStarted();
        while (stopWatch.getTime() <= Config.INSTANCE.actionTimeout) {
            try {
                List<WebElement> elements = ctx.findElements(selector);
                if (elements == null || elements.isEmpty()) {
                    sleep(Config.INSTANCE.defaultIterationTimeout);
                } else {
                    return elements;
                }
            } catch (Exception e) {
                sleep(Config.INSTANCE.defaultIterationTimeout);
            }
        }
        return ctx.findElements(selector);
    }
}
