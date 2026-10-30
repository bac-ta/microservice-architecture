package com.epl.gateway.models.responses;

import com.epl.gateway.rpc.RpcRoute;

/**
 * Entry of {@code GET /api/rpc}.
 *
 * @param path     where to POST the request
 * @param rpc      full gRPC method name
 * @param backend  backend service that answers it
 * @param type     UNARY, SERVER_STREAMING …
 * @param request  full name of the request message
 * @param response full name of the response message
 */
public record RpcDescription(String path, String rpc, String backend, String type, String request, String response) {

    public static RpcDescription of(RpcRoute route) {
        return new RpcDescription(
                "/rpc" + route.path(),
                route.fullMethodName(),
                route.backend(),
                route.type().name(),
                route.requestPrototype().getDescriptorForType().getFullName(),
                route.responsePrototype().getDescriptorForType().getFullName());
    }
}
