package com.epl.gateway.rpc;

import com.epl.gateway.configurations.GatewayProperties;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Calls backend RPCs with the forwarded metadata and the configured deadlines.
 */
@Component
public class RpcInvoker {

    private final ChannelProvider channels;
    private final Duration deadline;
    private final Duration streamDeadline;

    public RpcInvoker(ChannelProvider channels, GatewayProperties properties) {
        this.channels = channels;
        this.deadline = properties.deadline();
        this.streamDeadline = properties.streamDeadline();
    }

    public <Q, R> R unary(String backend, MethodDescriptor<Q, R> method, Q request, Metadata headers) {
        return ClientCalls.blockingUnaryCall(channel(backend, headers), method, options(deadline), request);
    }

    /**
     * Starts the call; the first {@link ServerStream#hasNext()} waits for the first response or the error.
     * The caller must close the stream.
     */
    public <Q, R> ServerStream<R> serverStreaming(String backend, MethodDescriptor<Q, R> method, Q request, Metadata headers) {
        var channel = channel(backend, headers);
        // the call is bound to the context current when it starts: cancelling it cancels the call
        var context = Context.current().withCancellation();
        var previous = context.attach();
        try {
            var responses = ClientCalls.blockingServerStreamingCall(channel, method, options(streamDeadline), request);
            return new ServerStream<>(responses, context);
        } catch (RuntimeException e) {
            context.cancel(e);
            throw e;
        } finally {
            context.detach(previous);
        }
    }

    /**
     * Sends the messages produced by {@code writer}, then waits for the single response.
     */
    public <Q, R> R clientStreaming(String backend, MethodDescriptor<Q, R> method, Metadata headers, RequestWriter<Q> writer)
            throws IOException {
        var call = channel(backend, headers).newCall(method, options(deadline));
        var response = new CompletableFuture<R>();
        var requests = ClientCalls.asyncClientStreamingCall(call, new StreamObserver<>() {
            @Override
            public void onNext(R value) {
                response.complete(value);
            }

            @Override
            public void onError(Throwable t) {
                response.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                response.completeExceptionally(Status.INTERNAL
                        .withDescription(method.getFullMethodName() + " completed without a response")
                        .asRuntimeException());
            }
        });

        try {
            writer.write(requests);
            requests.onCompleted();
        } catch (IOException | RuntimeException e) {
            requests.onError(Status.CANCELLED.withDescription("Request upload failed").withCause(e).asRuntimeException());
            throw e;
        }

        try {
            // the call deadline completes the future; the timeout here is only a safety net
            return response.get(deadline.toMillis() + 5_000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof StatusRuntimeException sre) {
                throw sre;
            }
            throw Status.fromThrowable(e.getCause()).asRuntimeException();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            call.cancel("interrupted", e);
            throw Status.CANCELLED.withCause(e).asRuntimeException();
        } catch (TimeoutException e) {
            call.cancel("deadline", e);
            throw Status.DEADLINE_EXCEEDED.withDescription(method.getFullMethodName()).asRuntimeException();
        }
    }

    private Channel channel(String backend, Metadata headers) {
        return ClientInterceptors.intercept(channels.channel(backend), MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    private static CallOptions options(Duration deadline) {
        return CallOptions.DEFAULT.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Writes the request messages of a client-streaming call.
     */
    @FunctionalInterface
    public interface RequestWriter<Q> {
        void write(StreamObserver<Q> requests) throws IOException;
    }
}
