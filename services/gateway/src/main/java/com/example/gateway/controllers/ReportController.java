package com.example.gateway.controllers;

import com.example.epl.report.v1.FileChunk;
import com.example.epl.report.v1.RenderRequest;
import com.example.epl.report.v1.ReportServiceGrpc;
import com.example.gateway.rpc.CallMetadata;
import com.example.gateway.rpc.ProtoJson;
import com.example.gateway.rpc.RpcInvoker;
import com.example.gateway.rpc.RpcRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Report download: epl.report.v1.ReportService/Render streamed as a file instead of NDJSON chunks.
 */
@RestController
public class ReportController {

    private static final String BACKEND = RpcRegistry.backendOf(ReportServiceGrpc.SERVICE_NAME);

    private final RpcInvoker invoker;

    public ReportController(RpcInvoker invoker) {
        this.invoker = invoker;
    }

    /**
     * @param body RenderRequest as JSON, e.g. {@code {"reportCode":"STOCK_LIST","format":"PDF","parameters":{...}}}
     */
    @PostMapping("/reports/render")
    public ResponseEntity<StreamingResponseBody> render(@RequestBody String body, HttpServletRequest request)
            throws InvalidProtocolBufferException {
        var renderRequest = ProtoJson.parse(body, RenderRequest.getDefaultInstance());
        var chunks = invoker.serverStreaming(BACKEND, ReportServiceGrpc.getRenderMethod(), renderRequest, CallMetadata.from(request));

        // file name and content type come with the first chunk
        FileChunk first;
        try {
            first = chunks.hasNext() ? chunks.next() : FileChunk.getDefaultInstance();
        } catch (RuntimeException e) {
            chunks.close();
            throw e;
        }

        StreamingResponseBody file = out -> {
            try (chunks) {
                first.getData().writeTo(out);
                while (chunks.hasNext()) {
                    chunks.next().getData().writeTo(out);
                }
            }
        };

        var fileName = first.getFileName().isEmpty()
                ? renderRequest.getReportCode() + "." + renderRequest.getFormat().toLowerCase(Locale.ROOT)
                : first.getFileName();
        var contentType = first.getContentType().isEmpty()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(first.getContentType());
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .body(file);
    }
}
