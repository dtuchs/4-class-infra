package com.dtuchs.libs.mock.base;

import com.dtuchs.libs.mock.base.allure.MockAttachment;
import io.qameta.allure.Step;
import io.qameta.allure.attachment.AttachmentData;
import io.qameta.allure.attachment.AttachmentProcessor;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.model.ExpectationId;
import org.mockserver.model.LogEventRequestAndResponse;
import org.mockserver.model.RequestDefinition;
import org.mockserver.verify.VerificationTimes;

import java.util.concurrent.CompletableFuture;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.HttpStatusCode.OK_200;

public class AllureMockClient extends MockServerClient {

    private static final String mockDataTemplatePath = "mock-data.ftl";

    private final AttachmentProcessor<AttachmentData> processor = new DefaultAttachmentProcessor();

    private boolean logToAllure = false;
    private boolean defaultResponseEnabled = false;

    public AllureMockClient(CompletableFuture<Integer> portFuture) {
        super(portFuture);
    }

    public AllureMockClient(String host, int port) {
        super(host, port);
    }

    public AllureMockClient(String host, int port, String contextPath) {
        super(host, port, contextPath);
    }

    public AllureMockClient withDefaultOKResponse() {
        defaultResponseEnabled = true;
        configureDefaultResponse();
        return this;
    }

    @Override
    public MockServerClient reset() {
        try {
            return super.reset();
        } finally {
            if (defaultResponseEnabled) {
                configureDefaultResponse();
            }
        }
    }

    public AllureMockClient enableAllureLog() {
        logToAllure = true;
        return this;
    }

    public AllureMockClient disableAllureLog() {
        logToAllure = false;
        return this;
    }

    public AllureMockClient saveRecordedRequestsAndResponses() {
        LogEventRequestAndResponse[] requestAndResponses = retrieveRecordedRequestsAndResponses(
                request()
        );
        MockAttachment attachment = new MockAttachment(requestAndResponses);
        processor.addAttachment(attachment, new FreemarkerAttachmentRenderer(mockDataTemplatePath));
        return this;
    }

    @Override
    @Step("Verify data in mock")
    public MockServerClient verify(RequestDefinition... requestDefinitions) throws AssertionError {
        if (logToAllure)
            saveRecordedRequestsAndResponses();
        return super.verify(requestDefinitions);
    }

    @Override
    @Step("Verify data in mock")
    public MockServerClient verify(ExpectationId... expectationIds) throws AssertionError {
        if (logToAllure)
            saveRecordedRequestsAndResponses();
        return super.verify(expectationIds);
    }

    @Override
    @Step("Verify data in mock")
    public MockServerClient verify(RequestDefinition requestDefinition, VerificationTimes times) throws AssertionError {
        if (logToAllure)
            saveRecordedRequestsAndResponses();
        return super.verify(requestDefinition, times);
    }

    @Override
    @Step("Verify data in mock")
    public MockServerClient verify(ExpectationId expectationId, VerificationTimes times) throws AssertionError {
        if (logToAllure)
            saveRecordedRequestsAndResponses();
        return super.verify(expectationId, times);
    }

    @Override
    @Step("Verify no requests have been sent.")
    public MockServerClient verifyZeroInteractions() throws AssertionError {
        if (logToAllure)
            saveRecordedRequestsAndResponses();
        return super.verifyZeroInteractions();
    }

    private void configureDefaultResponse() {
        when(request(), Times.unlimited(), TimeToLive.unlimited(), -10) // lowest priority
                .respond(response().withStatusCode(OK_200.code()).withBody("{\"status\":\"success\"}"));
    }
}
