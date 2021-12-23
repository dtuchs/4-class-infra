package com.dtuchs.libs.wiremock.base.allure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.qameta.allure.attachment.AttachmentData;

import java.util.List;

public class StubMappingAttachment implements AttachmentData {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String stubUrl;
    private final String method;
    private final List<String> stubBodyPatterns;

    private final int responseStatus;
    private final String responseBody;

    public StubMappingAttachment(String stubUrl, String method, List<String> stubBodyPatterns, int responseStatus, String responseBody) {
        this.stubUrl = stubUrl;
        this.method = method;
        this.stubBodyPatterns = stubBodyPatterns;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
    }

    public String getStubUrl() {
        return stubUrl;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getStubBodyPatterns() {
        return stubBodyPatterns;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getResponseBody() {
        return GSON.toJson(JsonParser.parseString(responseBody)); // pretty printing to report
    }

    @Override
    public String getName() {
        return "Stub mapping";
    }
}
