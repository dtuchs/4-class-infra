package com.dtuchs.libs.wiremock.base;

import com.dtuchs.libs.wiremock.base.allure.ServeEventAttachment;
import com.dtuchs.libs.wiremock.base.allure.StubMappingAttachment;
import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.qameta.allure.Step;
import io.qameta.allure.attachment.AttachmentData;
import io.qameta.allure.attachment.AttachmentProcessor;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;

import java.util.List;
import java.util.stream.Collectors;

public class AWiremock extends WireMock {

    private static final String serveEventTemplatePath = "serve-event.ftl";
    private static final String stubMappingTemplatePath = "stub-mapping.ftl";

    private final AttachmentProcessor<AttachmentData> processor = new DefaultAttachmentProcessor();

    private boolean saveServeEvents = false;
    private boolean autoConvertRawJson = false;
    private boolean saveStubMapping = false;

    public AWiremock(Admin admin) {
        super(admin);
    }

    public AWiremock(int port) {
        super(port);
    }

    public AWiremock(String host, int port) {
        super(host, port);
    }

    public AWiremock(String host, int port, String urlPathPrefix) {
        super(host, port, urlPathPrefix);
    }

    public AWiremock(String scheme, String host, int port) {
        super(scheme, host, port);
    }

    public AWiremock(String scheme, String host, int port, String urlPathPrefix) {
        super(scheme, host, port, urlPathPrefix);
    }

    public AWiremock(String scheme, String host, int port, String urlPathPrefix, String hostHeader, String proxyHost, int proxyPort, ClientAuthenticator authenticator) {
        super(scheme, host, port, urlPathPrefix, hostHeader, proxyHost, proxyPort, authenticator);
    }

    public AWiremock saveServeEvents(boolean save) {
        saveServeEvents = save;
        return this;
    }

    public AWiremock saveStubMappings(boolean save) {
        saveStubMapping = save;
        return this;
    }

    public AWiremock autoConvertBase64JsonForLogging(boolean convert) {
        autoConvertRawJson = convert;
        return this;
    }

    public AWiremock saveRecordedRequestsAndResponses(List<ServeEvent> serveEvents) {
        ServeEventAttachment attachment = new ServeEventAttachment(autoConvertRawJson, serveEvents.toArray(new ServeEvent[0]));
        processor.addAttachment(attachment, new FreemarkerAttachmentRenderer(serveEventTemplatePath));
        return this;
    }

    public AWiremock saveRecordedRequests(List<LoggedRequest> serveEvents) {
        ServeEventAttachment attachment = new ServeEventAttachment(autoConvertRawJson, serveEvents.toArray(new LoggedRequest[0]));
        processor.addAttachment(attachment, new FreemarkerAttachmentRenderer(serveEventTemplatePath));
        return this;
    }

    public AWiremock saveRecordedStubMapping(StubMapping stubMapping) {
        StubMappingAttachment attachment = new StubMappingAttachment(
                stubMapping.getRequest().getUrl(),
                stubMapping.getRequest().getMethod().getName(),
                stubMapping.getRequest().getBodyPatterns().stream().map(ContentPattern::toString).collect(Collectors.toList()),
                stubMapping.getResponse().getStatus(),
                stubMapping.getResponse().getBody()
        );
        processor.addAttachment(attachment, new FreemarkerAttachmentRenderer(stubMappingTemplatePath));
        return this;
    }

    @Override
    public StubMapping register(MappingBuilder mappingBuilder) {
        StubMapping mapping = mappingBuilder.build();
        this.register(mappingBuilder.build());
        return mapping;
    }

    @Override
    @Step("Register stub mapping")
    public void register(StubMapping mapping) {
        super.register(mapping);
        if (saveStubMapping)
            saveRecordedStubMapping(mapping);
    }

    @Override
    @Step("Verify requests")
    public void verifyThat(RequestPatternBuilder requestPatternBuilder) {
        if (saveServeEvents)
            saveRecordedRequestsAndResponses(super.getServeEvents());
        super.verifyThat(requestPatternBuilder);
    }

    @Override
    @Step("Verify requests")
    public void verifyThat(int expectedCount, RequestPatternBuilder requestPatternBuilder) {
        if (saveServeEvents)
            saveRecordedRequestsAndResponses(super.getServeEvents());
        super.verifyThat(expectedCount, requestPatternBuilder);
    }

    @Override
    @Step("Verify requests")
    public void verifyThat(CountMatchingStrategy expectedCount, RequestPatternBuilder requestPatternBuilder) {
        if (saveServeEvents)
            saveRecordedRequestsAndResponses(super.getServeEvents());
        super.verifyThat(expectedCount, requestPatternBuilder);
    }

    @Override
    @Step("Find all requests by pattern")
    public List<LoggedRequest> find(RequestPatternBuilder requestPatternBuilder) {
        List<LoggedRequest> loggedRequests = super.find(requestPatternBuilder);
        if (saveServeEvents)
            saveRecordedRequests(loggedRequests);
        return loggedRequests;
    }

    @Override
    @Step("Get all serve events")
    public List<ServeEvent> getServeEvents() {
        List<ServeEvent> serveEvents = super.getServeEvents();
        if (saveServeEvents)
            saveRecordedRequestsAndResponses(serveEvents);
        return serveEvents;
    }

    @Override
    @Step("Find all unmatched requests")
    public List<LoggedRequest> findAllUnmatchedRequests() {
        List<LoggedRequest> loggedRequests = super.findAllUnmatchedRequests();
        if (saveServeEvents)
            saveRecordedRequests(loggedRequests);
        return loggedRequests;
    }
}
