package com.example.gateway.controllers;

import com.example.epl.integration.v1.UploadChunk;
import com.example.epl.integration.v1.UploadServiceGrpc;
import com.example.gateway.rpc.CallMetadata;
import com.example.gateway.rpc.ProtoJson;
import com.example.gateway.rpc.RpcInvoker;
import com.example.gateway.rpc.RpcRegistry;
import com.google.protobuf.ByteString;
import jakarta.servlet.http.HttpServletRequest;
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
@RestController
public class UploadController {

    private static final int CHUNK_SIZE = 64 * 1024;
    private static final String BACKEND = RpcRegistry.backendOf(UploadServiceGrpc.SERVICE_NAME);

    private final RpcInvoker invoker;

    public UploadController(RpcInvoker invoker) {
        this.invoker = invoker;
    }

    /**
     * @param fileKind     ARRIVAL, SHIPMENT, PRODUCT …
     * @param validateOnly true = check the rows without registering them
     * @return UploadResult as JSON
     */
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("fileKind") String fileKind,
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
