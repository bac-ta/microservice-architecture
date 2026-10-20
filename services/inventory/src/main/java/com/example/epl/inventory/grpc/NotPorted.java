package com.example.epl.inventory.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

/**
 * Answer for RPCs whose legacy GWH logic has not been ported yet.
 */
final class NotPorted {

    private NotPorted() {
    }

    static StatusRuntimeException error(String fullMethodName, String legacy) {
        return Status.UNIMPLEMENTED
                .withDescription(fullMethodName + " is not implemented yet (legacy: " + legacy + ")")
                .asRuntimeException();
    }

    /** Request stream of a client-streaming RPC that already failed: drop whatever the client sends. */
    static <T> StreamObserver<T> ignoreRequests() {
        return new StreamObserver<>() {
            @Override
            public void onNext(T value) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}
