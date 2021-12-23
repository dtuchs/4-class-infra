package com.dtuchs.libs.grpc.base;

import io.grpc.stub.AbstractStub;

import java.util.Objects;

final class ConnConfig {

    public String grpcHost;
    public Integer grpcPort;
    public Boolean autoConvertRawJson = false;
    public Class<? extends AbstractStub<?>> stubClass;

    public ConnConfig validate() {
        if (grpcHost == null)
            throw new IllegalStateException("grpc host must not be null.");
        if (grpcPort == null)
            throw new IllegalStateException("grpc port must not be null.");
        if (stubClass == null)
            throw new IllegalStateException("Stub class must not be null.");
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnConfig that = (ConnConfig) o;
        return Objects.equals(grpcHost, that.grpcHost) && Objects.equals(grpcPort, that.grpcPort) && Objects.equals(autoConvertRawJson, that.autoConvertRawJson) && Objects.equals(stubClass, that.stubClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grpcHost, grpcPort, autoConvertRawJson, stubClass);
    }
}
