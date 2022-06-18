package com.dtuchs.libs.selenium.base;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static void sleep(long sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
