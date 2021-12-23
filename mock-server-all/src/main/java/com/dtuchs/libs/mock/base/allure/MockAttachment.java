package com.dtuchs.libs.mock.base.allure;

import io.qameta.allure.attachment.AttachmentData;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.LogEventRequestAndResponse;

import java.util.HashMap;
import java.util.Map;

public class MockAttachment implements AttachmentData {

    private final Map<HttpRequest, HttpResponse> stored = new HashMap<>();

    public MockAttachment(LogEventRequestAndResponse... events) {
        for (LogEventRequestAndResponse event : events) {
            HttpRequest request = (HttpRequest) event.getHttpRequest();
            HttpResponse response = event.getHttpResponse();
            stored.put(request, response);
        }
    }

    public Map<HttpRequest, HttpResponse> getStored() {
        return stored;
    }

    @Override
    public String getName() {
        return "Mock-server recorded requests & responses";
    }
}
