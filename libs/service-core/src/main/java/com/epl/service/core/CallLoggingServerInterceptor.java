package com.epl.service.core;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Outermost server interceptor: puts the request id (x-request-id, set by the gateway; generated when
 * missing) in the log MDC for every callback of the call, writes one log line per call (method, status,
 * duration) and turns an exception that escaped the service method into a generic INTERNAL.
 */
public class CallLoggingServerInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> REQUEST_ID = Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);

    /** MDC key, also used by the gateway: {@code logging.pattern.level: "%5p [%X{requestId:-}]"}. */
    public static final String MDC_REQUEST_ID = "requestId";

    private static final Logger logger = LoggerFactory.getLogger(CallLoggingServerInterceptor.class);

    // ids are logged as is: accept only short, plain values
    private static final Pattern VALID_REQUEST_ID = Pattern.compile("[A-Za-z0-9._:-]{1,64}");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        var method = call.getMethodDescriptor().getFullMethodName();
        var requestId = requestId(headers.get(REQUEST_ID));
        var loggedCall = new LoggedCall<>(call, method, requestId);

        ServerCall.Listener<ReqT> listener;
        try {
            listener = withMdc(requestId, () -> next.startCall(loggedCall, headers));
        } catch (RuntimeException e) {
            loggedCall.fail(e);
            return new ServerCall.Listener<>() {
            };
        }

        return new SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onMessage(ReqT message) {
                run(() -> super.onMessage(message));
            }

            @Override
            public void onHalfClose() {
                run(super::onHalfClose);
            }

            @Override
            public void onCancel() {
                run(super::onCancel);
            }

            @Override
            public void onComplete() {
                run(super::onComplete);
            }

            @Override
            public void onReady() {
                run(super::onReady);
            }

            private void run(Runnable callback) {
                try {
                    withMdc(requestId, () -> {
                        callback.run();
                        return null;
                    });
                } catch (RuntimeException e) {
                    loggedCall.fail(e);
                }
            }
        };
    }

    private static String requestId(String value) {
        return value != null && VALID_REQUEST_ID.matcher(value).matches() ? value : UUID.randomUUID().toString();
    }

    private static <T> T withMdc(String requestId, Supplier<T> body) {
        var previous = MDC.get(MDC_REQUEST_ID);
        MDC.put(MDC_REQUEST_ID, requestId);
        try {
            return body.get();
        } finally {
            if (previous == null) {
                MDC.remove(MDC_REQUEST_ID);
            } else {
                MDC.put(MDC_REQUEST_ID, previous);
            }
        }
    }

    private static final class LoggedCall<ReqT, RespT> extends SimpleForwardingServerCall<ReqT, RespT> {

        private final String method;
        private final String requestId;
        private final long startedAt = System.nanoTime();
        private volatile boolean closed;

        LoggedCall(ServerCall<ReqT, RespT> delegate, String method, String requestId) {
            super(delegate);
            this.method = method;
            this.requestId = requestId;
        }

        @Override
        public void close(Status status, Metadata trailers) {
            closed = true;
            withMdc(requestId, () -> {
                log(status);
                return null;
            });
            super.close(status, trailers);
        }

        /** Exception thrown by the service code instead of closing the call: logged, the client gets INTERNAL. */
        void fail(RuntimeException e) {
            withMdc(requestId, () -> {
                logger.error("{} failed", method, e);
                return null;
            });
            if (!closed) {
                close(Status.INTERNAL.withDescription("Internal error"), new Metadata());
            }
        }

        private void log(Status status) {
            var millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            switch (status.getCode()) {
                case INTERNAL, UNKNOWN, DATA_LOSS -> logger.warn("{} {} {}ms {}", method, status.getCode(), millis,
                        status.getDescription());
                default -> logger.info("{} {} {}ms", method, status.getCode(), millis);
            }
        }
    }
}
