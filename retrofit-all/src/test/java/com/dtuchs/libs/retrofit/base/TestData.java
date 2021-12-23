package com.dtuchs.libs.retrofit.base;

import java.util.Date;

public class TestData {
    private Date dateTime;

    public TestData(Date date) {
        this.dateTime = date;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
