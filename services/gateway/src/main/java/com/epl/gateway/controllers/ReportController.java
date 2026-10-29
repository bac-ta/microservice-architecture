package com.epl.gateway.controllers;

import com.epl.report.v1.FileChunk;
import com.epl.report.v1.RenderRequest;
import com.epl.report.v1.ReportServiceGrpc;
import com.epl.gateway.configurations.OpenApiConfiguration;
import com.epl.gateway.configurations.TenantScoped;
import com.epl.gateway.rpc.CallMetadata;
import com.epl.gateway.rpc.ProtoJson;
import com.epl.gateway.rpc.RpcInvoker;
import com.epl.gateway.rpc.RpcRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Files", description = "Report downloads and file uploads")
@RequiredArgsConstructor
@RestController
public class ReportController {

    private static final String BACKEND = RpcRegistry.backendOf(ReportServiceGrpc.SERVICE_NAME);

    private final RpcInvoker invoker;

    /**
     * @param body RenderRequest as JSON, e.g. {@code {"reportCode":"STOCK_LIST","format":"PDF","parameters":{...}}}
     */
    @Operation(summary = "Render a report as a file download",
            description = "epl.report.v1.ReportService/Render, its chunks joined into one file. "
                    + "File name and content type come from the report service.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(ref = "#/components/schemas/epl.report.v1.RenderRequest"),
                    examples = @ExampleObject("{\"reportCode\": \"STOCK_LIST\", \"format\": \"PDF\", \"parameters\": {}}"))))
    @ApiResponse(responseCode = "200", description = "The rendered file (PDF, XLSX, CSV …)",
            headers = @Header(name = HttpHeaders.CONTENT_DISPOSITION, description = "attachment; filename=…"),
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "default", description = "gRPC status mapped to HTTP",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(ref = OpenApiConfiguration.API_ERROR_REF)))
    @TenantScoped
    @PostMapping(value = "/reports/render", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> render(@RequestBody @Parameter(hidden = true) String body, HttpServletRequest request)
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
