package com.dtuchs.libs.selenium.base;

import com.dtuchs.libs.selenium.base.config.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class WebDriverFactory {

    /**
     * @throws IllegalStateException if configured webdriver not supported
     */
    @Nonnull
    public static WebDriver createWebDriver() {
        switch (Config.INSTANCE.browser) {
            case CHROME: {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.setHeadless(Config.INSTANCE.headless);
                options.setExperimentalOption("prefs", chromePrefs());
                options.addArguments("--no-sandbox", "--ignore-ssl-errors=yes", "--ignore-certificate-errors");
                WebDriver driver = new ChromeDriver(options);
                driver.manage().window().maximize();
                return driver;
            }
            default: {
                throw new IllegalStateException("Browser not supported");
            }
        }
    }

    @Nonnull
    private static Map<String, Object> chromePrefs() {
        Map<String, Object> chromePreferences = new HashMap<>();
        chromePreferences.put("safebrowsing.enabled", true);
        chromePreferences.put("credentials_enable_service", false);
        chromePreferences.put("plugins.always_open_pdf_externally", true);
        chromePreferences.put("profile.default_content_setting_values.automatic_downloads", 1);
        return chromePreferences;
    }
}
