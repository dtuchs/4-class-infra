package com.dtuchs.libs.grpc.base;

import io.grpc.stub.AbstractStub;

public class GrpcStubBuilder {

    private final ConnConfig config = new ConnConfig();

    public <T extends AbstractStub<T>> GrpcStubBuilder forStub(Class<T> stubClass) {
        config.stubClass = stubClass;
        return this;
    }

    public GrpcStubBuilder withHost(String grpcHost) {
        config.grpcHost = grpcHost;
        return this;
    }

    public GrpcStubBuilder withPort(int grpcPort) {
        config.grpcPort = grpcPort;
        return this;
    }

    /**
     * Build blocking / async stub (depends on given stubClass)
     */
    public <T extends AbstractStub<T>> T build() {
        return GrpcStubContext.INSTANCE.get(config.validate());
    }
}
