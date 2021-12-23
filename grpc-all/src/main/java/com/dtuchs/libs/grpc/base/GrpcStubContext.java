package com.dtuchs.libs.grpc.base;

import com.dtuchs.libs.grpc.base.interceptor.ConsoleInterceptor;
import com.dtuchs.libs.grpc.base.interceptor.AllureInterceptor;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractAsyncStub;
import io.grpc.stub.AbstractBlockingStub;
import io.grpc.stub.AbstractStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
enum GrpcStubContext {
    INSTANCE;

    private static final String
            CREATE_BLOCKING_STUB_METHOD_NAME = "newBlockingStub",
            CREATE_STUB_METHOD_NAME = "newStub";

    private Logger log = LoggerFactory.getLogger(GrpcStubContext.class);
    private final Map<ConnConfig, ? super AbstractStub<?>> container = new HashMap<>();

    /**
     * Get existing or compute a new Blocking / Async Stub for given configuration.
     */
    synchronized <T extends AbstractStub<T>> T get(ConnConfig connConfig) {
        if (container.containsKey(connConfig))
            return (T) container.get(connConfig);
        else {
            T stub = createStub(connConfig);
            container.put(connConfig, stub);
            return stub;
        }
    }

    private <T extends AbstractStub<T>> T createStub(ConnConfig connConfig) {
        try {
            Method m = connConfig.stubClass
                    .getDeclaringClass() // outer parent class
                    .getMethod(resolveNewInstanceMethod(connConfig.stubClass), io.grpc.Channel.class);

            return ((T) m.invoke(null, ChannelFactory.getChannel(connConfig)))
                    .withInterceptors(new ConsoleInterceptor(), new AllureInterceptor(connConfig.autoConvertRawJson));
        } catch (Exception e) {
            throw new IllegalStateException("Can`t create blockingStub", e);
        }
    }

    private String resolveNewInstanceMethod(Class<? extends AbstractStub<?>> stubClass) {
        if (AbstractBlockingStub.class.isAssignableFrom(stubClass)) {
            return CREATE_BLOCKING_STUB_METHOD_NAME;
        } else if (AbstractAsyncStub.class.isAssignableFrom(stubClass)) {
            return CREATE_STUB_METHOD_NAME;
        } else throw new IllegalArgumentException("Stub class must extends AbstractBlockingStub or AbstractAsyncStub");
    }

    static class ChannelFactory {
        /**
         * For tests only.
         */
        private static Channel testChannel;

        /**
         * For tests only.
         */
        static void setMockChannel(Channel channel) {
            testChannel = channel;
        }

        private static Channel getChannel(ConnConfig connConfig) {
            return testChannel == null
                    ? ManagedChannelBuilder
                    .forAddress(connConfig.grpcHost, connConfig.grpcPort)
                    .usePlaintext()
                    .build()
                    : testChannel;
        }
    }
}
