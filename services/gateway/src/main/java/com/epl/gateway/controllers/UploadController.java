package com.epl.gateway.controllers;

import com.epl.integration.v1.UploadChunk;
import com.epl.integration.v1.UploadServiceGrpc;
import com.epl.gateway.configurations.OpenApiConfiguration;
import com.epl.gateway.configurations.TenantScoped;
import com.epl.gateway.rpc.CallMetadata;
import com.epl.gateway.rpc.ProtoJson;
import com.epl.gateway.rpc.RpcInvoker;
import com.epl.gateway.rpc.RpcRegistry;
import com.google.protobuf.ByteString;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

/**
 * File upload (arrival / shipment / product master files …) as multipart, streamed to
 * epl.integration.v1.UploadService/Upload in chunks.
 */
@Tag(name = "Files", description = "Report downloads and file uploads")
@RequiredArgsConstructor
@RestController
public class UploadController {

    private static final int CHUNK_SIZE = 64 * 1024;
    private static final String BACKEND = RpcRegistry.backendOf(UploadServiceGrpc.SERVICE_NAME);

    private final RpcInvoker invoker;

    /**
     * @param fileKind     ARRIVAL, SHIPMENT, PRODUCT …
     * @param validateOnly true = check the rows without registering them
     * @return UploadResult as JSON
     */
    @Operation(summary = "Upload a file (arrivals, shipments, product master …)",
            description = "Streamed in 64 KiB chunks to epl.integration.v1.UploadService/Upload.")
    @ApiResponse(responseCode = "200", description = "Rows read, registered and rejected",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(ref = "#/components/schemas/epl.integration.v1.UploadResult")))
    @ApiResponse(responseCode = "default", description = "gRPC status mapped to HTTP",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(ref = OpenApiConfiguration.API_ERROR_REF)))
    @TenantScoped
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@Parameter(description = "The file to import")
                                         @RequestParam("file") MultipartFile file,
                                         @Parameter(description = "ARRIVAL, SHIPMENT, PRODUCT …", example = "ARRIVAL")
                                         @RequestParam("fileKind") String fileKind,
                                         @Parameter(description = "true = check the rows without registering them")
                                         @RequestParam(value = "validateOnly", defaultValue = "false") boolean validateOnly,
                                         HttpServletRequest request) throws IOException {
        var header = UploadChunk.Header.newBuilder()
                .setFileKind(fileKind)
                .setFileName(Objects.requireNonNullElse(file.getOriginalFilename(), ""))
                .setContentType(Objects.requireNonNullElse(file.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .setValidateOnly(validateOnly)
                .build();

        var result = invoker.clientStreaming(BACKEND, UploadServiceGrpc.getUploadMethod(), CallMetadata.from(request), chunks -> {
            chunks.onNext(UploadChunk.newBuilder().setHeader(header).build());
            try (var in = file.getInputStream()) {
                var buffer = new byte[CHUNK_SIZE];
                int read;
                while ((read = in.readNBytes(buffer, 0, CHUNK_SIZE)) > 0) {
                    chunks.onNext(UploadChunk.newBuilder().setData(ByteString.copyFrom(buffer, 0, read)).build());
                }
            }
        });

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(ProtoJson.print(result));
    }
}
