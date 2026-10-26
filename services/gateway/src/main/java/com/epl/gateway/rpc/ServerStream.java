package com.epl.gateway.rpc;

import io.grpc.Context;

import java.util.Iterator;

/**
 * Responses of a server-streaming call. {@link #close()} cancels the call if it has not finished,
 * e.g. when the HTTP client disconnects in the middle of an export.
 * Not thread-safe: read it from one thread at a time.
 */
public final class ServerStream<T> implements Iterator<T>, AutoCloseable {

    private final Iterator<T> responses;
    private final Context.CancellableContext context;

    ServerStream(Iterator<T> responses, Context.CancellableContext context) {
        this.responses = responses;
        this.context = context;
    }

    /**
     * @throws io.grpc.StatusRuntimeException when the backend fails the call
     */
    @Override
    public boolean hasNext() {
        return responses.hasNext();
    }

    @Override
    public T next() {
        return responses.next();
    }

    @Override
    public void close() {
        context.cancel(null);
    }
}
