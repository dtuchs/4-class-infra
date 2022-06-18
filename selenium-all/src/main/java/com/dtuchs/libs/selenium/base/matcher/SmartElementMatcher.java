package com.dtuchs.libs.selenium.base.matcher;

import com.dtuchs.libs.selenium.base.WebDriverContainer;
import com.dtuchs.libs.selenium.base.config.Config;
import org.apache.commons.lang3.time.StopWatch;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.dtuchs.libs.selenium.base.Utils.sleep;

public class SmartElementMatcher {

    private final WebElement matchedElement;

    private SmartElementMatcher(WebElement matchedElement) {
        this.matchedElement = matchedElement;
    }

    @Nonnull
    public static SmartElementMatcher assertThat(@Nonnull WebElement element) {
        return new SmartElementMatcher(element);
    }

    @Nonnull
    public SmartElementMatcher isVisible() {
        flexCheck(webElement -> Assertions.assertThat(webElement.isDisplayed())
                .isTrue());
        return this;
    }

    @Nonnull
    public SmartElementMatcher isDisabled() {
        flexCheck(webElement -> Assertions.assertThat(webElement.isEnabled())
                .isFalse());
        return this;
    }

    @Nonnull
    public SmartElementMatcher isNotVisible() {
        flexCheck(webElement -> Assertions.assertThat(webElement.isDisplayed())
                .isFalse());
        return this;
    }

    @Nonnull
    public SmartElementMatcher hasText(@Nullable String expectedText) {
        flexCheck(webElement -> Assertions.assertThat(webElement.getText())
                .isEqualTo(expectedText));
        return this;
    }

    @Nonnull
    public SmartElementMatcher hasTexts(String... expectedTexts) {
        flexCheck(webElement -> Assertions.assertThat(webElement.getText())
                .contains(expectedTexts));
        return this;
    }

    @Nonnull
    public SmartElementMatcher hasNoText(@Nullable String expectedText) {
        flexCheck(webElement -> Assertions.assertThat(webElement.getText())
                .isNotEqualTo(expectedText));
        return this;
    }

    @Nonnull
    public SmartElementMatcher containsText(@Nullable String expectedText) {
        flexCheck(webElement -> Assertions.assertThat(webElement.getText())
                .contains(expectedText));
        return this;
    }

    @Nonnull
    public SmartElementMatcher containsIgnoringCaseText(@Nullable String expectedText) {
        flexCheck(webElement -> Assertions.assertThat(webElement.getText())
                .containsIgnoringCase(expectedText));
        return this;
    }

    @Nonnull
    public SmartElementMatcher hasAttribute(@Nullable String attributeName, @Nullable String expectedValue) {
        flexCheck(webElement -> Assertions.assertThat(webElement.getAttribute(attributeName))
                .contains(expectedValue));
        return this;
    }

    private void flexCheck(@Nonnull Consumer<WebElement> action) {
        StopWatch stopWatch = StopWatch.createStarted();
        boolean scrolled = false;
        while (stopWatch.getTime() <= Config.INSTANCE.actionTimeout) {
            try {
                if (!scrolled) {
                    ((JavascriptExecutor) WebDriverContainer.INSTANCE.getRequiredWebDriver())
                            .executeScript("arguments[0].scrollIntoView(false)", matchedElement);
                    scrolled = true;
                }
                action.accept(matchedElement);
                return;
            } catch (Throwable e) {
                sleep(Config.INSTANCE.defaultIterationTimeout);
            }
        }
        action.accept(matchedElement);
    }
}
