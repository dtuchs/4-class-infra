package com.dtuchs.libs.wiremock.base.allure;

import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.qameta.allure.attachment.AttachmentData;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ServeEventAttachment implements AttachmentData {

    private static final Gson GSON = new Gson();
    private final Map<LoggedRequest, LoggedResponse> stored = new HashMap<>();

    public ServeEventAttachment(boolean autoConvertRawJson, ServeEvent... events) {
        for (ServeEvent event : events) {
            LoggedRequest request = autoConvertRawJson
                    ? convertRawJson(event.getRequest())
                    : event.getRequest();
            stored.put(request, event.getResponse());
        }
    }

    public ServeEventAttachment(boolean autoConvertRawJson, LoggedRequest... events) {
        for (LoggedRequest event : events) {
            LoggedRequest request = autoConvertRawJson
                    ? convertRawJson(event)
                    : event;
            stored.put(request, null);
        }
    }

    public Map<LoggedRequest, LoggedResponse> getStored() {
        return stored;
    }

    private LoggedRequest convertRawJson(LoggedRequest request) {
        JsonObject sourceObj = GSON.fromJson(request.getBodyAsString(), JsonObject.class);
        if (!sourceObj.has("rawJson")) {
            return request;
        }
        JsonObject decodedRawJson = GSON.fromJson(new String(Base64.getDecoder().decode(sourceObj.get("rawJson").getAsString())), JsonObject.class);
        sourceObj.add("rawJson", decodedRawJson);
        return new LoggedRequest(
                request.getUrl(),
                request.getAbsoluteUrl(),
                request.getMethod(),
                request.getClientIp(),
                request.getHeaders(),
                request.getCookies(),
                request.isBrowserProxyRequest(),
                request.getLoggedDate(),
                sourceObj.toString().getBytes(StandardCharsets.UTF_8),
                request.getParts()
        );
    }

    @Override
    public String getName() {
        if (stored.values().stream().allMatch(Objects::isNull))
            return "Wiremock recorded requests";
        else
            return "Wiremock recorded requests & responses";
    }
}
