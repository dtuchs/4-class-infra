package com.dtuchs.libs.selenium.base;

import com.dtuchs.libs.selenium.base.matcher.SmartElementListMatcher;
import org.junit.jupiter.api.Test;

class WebTest {
    @Test
    void webTest() {
        Core.navigate("https://github.com/dtuchs");
        SmartElementListMatcher
                .assertThat(Core.locateAll("li.flex-content-stretch"))
                .containsTextInAnyElement("heisenbug-2021-piter");
    }
}