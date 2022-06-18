package com.dtuchs.libs.selenium.base;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;

public class Core {

    public static void open(@Nonnull String url) {
        WebDriverContainer.INSTANCE.getOrInitWebDriver().navigate().to(url);
    }

    public static void navigate(@Nonnull String url) {
        open(url);
    }

    public static void close() {
        WebDriverContainer.INSTANCE.closeWebDriver();
    }

    @Nonnull
    public static WebElement locate(@Nonnull String cssSelector) {
        By locator = By.cssSelector(cssSelector);
        return new SmartElement(locator);
    }

    @Nonnull
    public static WebElement locateX(@Nonnull String xpathSelector) {
        By locator = By.xpath(xpathSelector);
        return new SmartElement(locator);
    }

    @Nonnull
    public static SmartElementList locateAll(@Nonnull String cssSelector) {
        By locator = By.cssSelector(cssSelector);
        return new SmartElementList(locator);
    }

    @Nonnull
    public static SmartElementList locateAllX(@Nonnull String xpathSelector) {
        By locator = By.xpath(xpathSelector);
        return new SmartElementList(locator);
    }

    public static void deleteCookie(@Nonnull Cookie cookie) {
        WebDriverContainer.INSTANCE.getRequiredWebDriver()
                .manage()
                .deleteCookieNamed(cookie.getName());
    }

    public static void addCookie(@Nonnull Cookie cookie) {
        WebDriverContainer.INSTANCE.getRequiredWebDriver()
                .manage()
                .addCookie(cookie);

    }
}
