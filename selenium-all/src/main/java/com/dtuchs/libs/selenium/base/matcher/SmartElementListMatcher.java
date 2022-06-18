package com.dtuchs.libs.selenium.base.matcher;

import com.dtuchs.libs.selenium.base.WebDriverContainer;
import com.dtuchs.libs.selenium.base.config.Config;
import org.apache.commons.lang3.time.StopWatch;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.dtuchs.libs.selenium.base.Utils.sleep;

public class SmartElementListMatcher {

    private final List<WebElement> matchedList;

    private SmartElementListMatcher(List<WebElement> matchedList) {
        this.matchedList = matchedList;
    }

    @Nonnull
    public static SmartElementListMatcher assertThat(@Nonnull List<WebElement> list) {
        return new SmartElementListMatcher(list);
    }

    @Nonnull
    public SmartElementListMatcher hasSizeGreaterThan(int expectedSize) {
        flexCheck(webElements -> Assertions.assertThat(webElements)
                .hasSizeGreaterThan(expectedSize));
        return this;
    }

    @Nonnull
    public SmartElementListMatcher checkTextsOrder(Comparator<String> comparator) {
        flexCheck(webElements -> Assertions.assertThat(webElements)
                .extracting((Function<WebElement, String>) WebElement::getText)
                .isSortedAccordingTo(comparator));
        return this;
    }

    @Nonnull
    public SmartElementListMatcher containsTextsInAnyOrder(@Nonnull String... expectedTexts) {
        flexCheck(webElements -> Assertions.assertThat(webElements)
                .hasSizeGreaterThan(0)
                .extracting((Function<WebElement, String>) WebElement::getText)
                .containsExactlyInAnyOrder(expectedTexts));
        return this;
    }

    @Nonnull
    public SmartElementListMatcher containsTextInAnyElement(@Nonnull String expectedText) {
        flexCheck(webElements ->
                Assertions.assertThat(
                        webElements
                                .stream()
                                .map(WebElement::getText)
                                .anyMatch(
                                        text -> text.toLowerCase(Locale.ROOT).contains(expectedText.toLowerCase(Locale.ROOT))
                                )).isTrue()
        );
        return this;
    }

    private void flexCheck(@Nonnull Consumer<List<WebElement>> action) {
        StopWatch stopWatch = StopWatch.createStarted();
        boolean scrolled = false;
        while (stopWatch.getTime() <= Config.INSTANCE.actionTimeout) {
            try {
                if (!scrolled) {
                    ((JavascriptExecutor) WebDriverContainer.INSTANCE.getRequiredWebDriver())
                            .executeScript("arguments[0].scrollIntoView(false)", matchedList.get(0));
                    scrolled = true;
                }
                action.accept(matchedList);
                return;
            } catch (Throwable e) {
                sleep(Config.INSTANCE.defaultIterationTimeout);
            }
        }
        action.accept(matchedList);
    }
}
