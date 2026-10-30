package com.epl.gateway.controllers;

import com.epl.gateway.configurations.RequestIdFilter;
import com.epl.gateway.models.responses.ApiError;
import com.epl.gateway.models.responses.RpcDescription;
import com.epl.gateway.rpc.CallMetadata;
import com.epl.gateway.rpc.ProtoJson;
import com.epl.gateway.rpc.RpcInvoker;
import com.epl.gateway.rpc.RpcRegistry;
import com.epl.gateway.rpc.RpcRoute;
import com.epl.gateway.rpc.ServerStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Every backend RPC as JSON over HTTP: {@code POST /api/rpc/{backend}/{version}/{service}/{method}} in lowercase
 * words joined by '-' (see {@link RpcRegistry#pathOf(String)}), body = request message in proto3 JSON, e.g.
 * {@code POST /api/rpc/masterdata/v1/product/get-product  {"code":"P001"}} for epl.masterdata.v1.ProductService/GetProduct.
 * <ul>
 *     <li>unary: response message as JSON</li>
 *     <li>server-streaming: one JSON message per line (application/x-ndjson); an error after the first line
 *     arrives as a last line {@code {"error":{...}}}</li>
 *     <li>client-streaming: not mapped, see {@link UploadController}</li>
 * </ul>
 */
@Tag(name = "Gateway", description = "What the gateway exposes")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rpc")
public class RpcController {

    private static final Logger logger = LoggerFactory.getLogger(RpcController.class);

    private final RpcRegistry registry;
    private final RpcInvoker invoker;
    private final ObjectMapper objectMapper;

    @Operation(summary = "List every RPC exposed under /rpc",
            description = "Path, backend, call type and message names of each RPC.")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RpcDescription> list() {
        return registry.routes().stream().map(RpcDescription::of).toList();
    }

    // documented per RPC by OpenApiConfiguration
    @Hidden
    @PostMapping("/{*path}")
    public ResponseEntity<String> call(@PathVariable String path,
                                       @RequestBody(required = false) String body,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        var route = registry.find(path).orElseThrow(() -> Status.NOT_FOUND
                .withDescription("Unknown RPC /rpc" + path + " (GET /api/rpc lists them)")
                .asRuntimeException());
        var message = ProtoJson.parse(body, route.requestPrototype());
        var metadata = CallMetadata.from(request);

        return switch (route.type()) {
            case UNARY -> ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ProtoJson.print(invoker.unary(route.backend(), route.method(), message, metadata)));
            case SERVER_STREAMING -> {
                writeStream(route, invoker.serverStreaming(route.backend(), route.method(), message, metadata),
                        RequestIdFilter.requestId(request), response);
                yield null;
            }
            default -> throw Status.UNIMPLEMENTED
                    .withDescription(route.fullMethodName() + " is " + route.type() + " and has no JSON mapping")
                    .asRuntimeException();
        };
    }

    /**
     * Written on the request thread (a virtual thread, see spring.threads.virtual.enabled), flushed per message.
     * If the client disconnects, the write fails and closing the stream cancels the backend call.
     */
    private void writeStream(RpcRoute route, ServerStream<Message> responses, String requestId, HttpServletResponse response)
            throws IOException {
        try (responses) {
            // wait for the first response, so that an immediate failure still gets its HTTP status
            responses.hasNext();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_NDJSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            var out = response.getOutputStream();
            try {
                while (responses.hasNext()) {
                    writeLine(out, ProtoJson.print(responses.next()));
                }
            } catch (StatusRuntimeException e) {
                logger.warn("{} failed after the response started: {}", route.fullMethodName(), e.getStatus());
                writeLine(out, objectMapper.writeValueAsString(Map.of("error", ApiError.of(e.getStatus(), requestId))));
            }
        }
    }

    private static void writeLine(OutputStream out, String json) throws IOException {
        out.write(json.getBytes(StandardCharsets.UTF_8));
        out.write('\n');
        out.flush();
    }
}
