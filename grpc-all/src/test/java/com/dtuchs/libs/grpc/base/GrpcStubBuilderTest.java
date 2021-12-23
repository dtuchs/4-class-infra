package com.dtuchs.libs.grpc.base;

import com.propellerads.libs.grpc.base.proto.Request;
import com.propellerads.libs.grpc.base.proto.Response;
import com.propellerads.libs.grpc.base.proto.TestServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;
import org.grpcmock.GrpcMock;
import org.grpcmock.junit5.GrpcMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.grpcmock.GrpcMock.unaryMethod;

@ExtendWith(GrpcMockExtension.class)
class GrpcStubBuilderTest {

    ManagedChannel channel;

    @BeforeEach
    void setupChannel() {
        channel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort())
                .usePlaintext()
                .build();
        GrpcStubContext.ChannelFactory.setMockChannel(channel);
    }

    @ValueSource(classes = {TestServiceGrpc.TestServiceBlockingStub.class, TestServiceGrpc.TestServiceStub.class})
    @ParameterizedTest
    <T extends AbstractStub<T>> void blockingStubShouldReturnedForGiven(Class<T> stubClass) {
        AbstractStub stub = new GrpcStubBuilder().forStub(stubClass)
                .withHost("localhost")
                .withPort(GrpcMock.getGlobalPort())
                .build();

        Assertions.assertEquals(stubClass, stub.getClass());
    }

    @Test
    void testStub() {
        GrpcMock.stubFor(unaryMethod(TestServiceGrpc.getCalculateMethod())
                .willReturn(Response.newBuilder().build()));

        TestServiceGrpc.TestServiceBlockingStub stub = new GrpcStubBuilder().forStub(TestServiceGrpc.TestServiceBlockingStub.class)
                .withHost("localhost")
                .withPort(GrpcMock.getGlobalPort())
                .build();

        Assertions.assertDoesNotThrow(() ->
                stub.calculate(Request.newBuilder().setTopic("topic").build())
        );
    }

    @AfterEach
    void shutdownChannel() {
        GrpcStubContext.ChannelFactory.setMockChannel(null);
        Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
    }
}