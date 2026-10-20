package com.example.gateway.rpc;

import io.grpc.Channel;

/**
 * gRPC channel of a backend service, by backend name (auth, masterdata, inventory …).
 */
public interface ChannelProvider {

    /**
     * @throws io.grpc.StatusRuntimeException UNAVAILABLE when no target is configured for the backend
     */
    Channel channel(String backend);
}
