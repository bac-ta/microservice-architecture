package com.example.epl.billing.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

/**
 * Answer for RPCs that are not implemented yet.
 */
final class NotPorted {

    private NotPorted() {
    }

    /** @param screens screen codes that call the RPC (docs/screen-codes.md); none = shared by every screen */
    static StatusRuntimeException error(String fullMethodName, String... screens) {
        var description = fullMethodName + " is not implemented yet";
        if (screens.length > 0) {
            description += " (screens: " + String.join(", ", screens) + ")";
        }
        return Status.UNIMPLEMENTED.withDescription(description).asRuntimeException();
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
