package com.dtuchs.libs.grpc.base.interceptor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.qameta.allure.Allure;
import io.qameta.allure.attachment.AttachmentData;
import io.qameta.allure.attachment.AttachmentProcessor;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.util.ResultsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class AllureInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AllureInterceptor.class);
    private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer();
    private static final Gson GSON = new Gson();

    private String requestTemplatePath = "grpc-request.ftl";
    private String responseTemplatePath = "grpc-response.ftl";
    private final boolean autoConvertRawJson;

    public AllureInterceptor(boolean autoConvertRawJson) {
        this.autoConvertRawJson = autoConvertRawJson;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions, Channel channel) {
        final AttachmentProcessor<AttachmentData> processor = new DefaultAttachmentProcessor();

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                channel.newCall(methodDescriptor, callOptions.withoutWaitForReady())) {

            private String stepUuid;
            private List<String> parsedResponses = new ArrayList<>();

            @Override
            public void sendMessage(ReqT message) {
                final String uuid = UUID.randomUUID().toString();
                stepUuid = uuid;
                Allure.getLifecycle().startStep(uuid, (new StepResult()).setName(
                        "Send gRPC request to "
                                + channel.authority()
                                + trimGrpcMethodName(methodDescriptor.getFullMethodName())
                ));
                try {
                    String bodyAsJson = jsonPrinter.print((MessageOrBuilder) message);
                    if (autoConvertRawJson) {
                        JsonObject sourceObj = GSON.fromJson(bodyAsJson, JsonObject.class);
                        if (sourceObj.has("rawJson")) {
                            JsonObject decodedRawJson = GSON.fromJson(new String(Base64.getDecoder().decode(sourceObj.get("rawJson").getAsString())), JsonObject.class);
                            sourceObj.add("rawJson", decodedRawJson);
                            bodyAsJson = sourceObj.toString();
                        }
                    }

                    GrpcRequestAttachment rpcRequestAttach = GrpcRequestAttachment.Builder
                            .create("gRPC request", methodDescriptor.getFullMethodName())
                            .setBody(bodyAsJson)
                            .build();
                    processor.addAttachment(rpcRequestAttach, new FreemarkerAttachmentRenderer(requestTemplatePath));
                    super.sendMessage(message);
                } catch (InvalidProtocolBufferException e) {
                    log.warn("Can`t parse gRPC request", e);
                } catch (Throwable e) {
                    Allure.getLifecycle().updateStep((s) -> {
                        s.setStatus(ResultsUtils.getStatus(e).orElse(Status.BROKEN)).setStatusDetails(ResultsUtils.getStatusDetails(e).orElse(null));
                    });
                    Allure.getLifecycle().stopStep(stepUuid);
                    stepUuid = null;
                }
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                ClientCall.Listener<RespT> listener = new ForwardingClientCallListener<RespT>() {
                    @Override
                    protected Listener<RespT> delegate() {
                        return responseListener;
                    }

                    @Override
                    public void onClose(io.grpc.Status status, Metadata trailers) {
                        if (parsedResponses.size() == 1) {
                            GrpcResponseAttachment rpcResponseAttach = GrpcResponseAttachment.Builder
                                    .create("gRPC response")
                                    .setBody(parsedResponses.iterator().next())
                                    .build();
                            processor.addAttachment(rpcResponseAttach, new FreemarkerAttachmentRenderer(responseTemplatePath));
                        } else if (parsedResponses.size() > 1) {
                            GrpcResponseAttachment rpcResponseAttach = GrpcResponseAttachment.Builder
                                    .create("gRPC response (each object as array element)")
                                    .setBody("[" + String.join(",\n", parsedResponses) + "]")
                                    .build();
                            processor.addAttachment(rpcResponseAttach, new FreemarkerAttachmentRenderer(responseTemplatePath));
                        }

                        if (status.isOk()) {
                            Allure.getLifecycle().updateStep(stepUuid, (step) -> step.setStatus(Status.PASSED));
                        } else {
                            Allure.getLifecycle().updateStep(stepUuid, (step) -> step.setStatus(Status.FAILED));
                        }
                        Allure.getLifecycle().stopStep(stepUuid);
                        stepUuid = null;
                        super.onClose(status, trailers);
                    }

                    @Override
                    public void onMessage(RespT message) {
                        try {
                            parsedResponses.add(jsonPrinter.print((MessageOrBuilder) message));
                            super.onMessage(message);
                        } catch (InvalidProtocolBufferException e) {
                            log.warn("Can`t parse gRPC response", e);
                        } catch (Throwable e) {
                            Allure.getLifecycle().updateStep((s) -> {
                                s.setStatus(ResultsUtils.getStatus(e).orElse(Status.BROKEN)).setStatusDetails(ResultsUtils.getStatusDetails(e).orElse(null));
                            });
                            Allure.getLifecycle().stopStep(stepUuid);
                            stepUuid = null;
                            return;
                        }
                    }
                };
                super.start(listener, headers);
            }

            private String trimGrpcMethodName(String source) {
                return source.substring(source.lastIndexOf("/"));
            }
        };
    }
}
