package com.example.gateway;

import com.example.epl.auth.v1.AuthServiceGrpc;
import com.example.epl.auth.v1.JwksResponse;
import com.example.epl.integration.v1.UploadChunk;
import com.example.epl.integration.v1.UploadResult;
import com.example.epl.integration.v1.UploadServiceGrpc;
import com.example.epl.inventory.v1.SearchStockRequest;
import com.example.epl.inventory.v1.StockLot;
import com.example.epl.inventory.v1.StockServiceGrpc;
import com.example.epl.masterdata.v1.GetProductRequest;
import com.example.epl.masterdata.v1.Product;
import com.example.epl.masterdata.v1.ProductServiceGrpc;
import com.example.epl.report.v1.FileChunk;
import com.example.epl.report.v1.RenderRequest;
import com.example.epl.report.v1.ReportServiceGrpc;
import com.example.gateway.rpc.ChannelProvider;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-process gRPC server standing in for every backend; all backends share its channel.
 */
@TestConfiguration
public class FakeBackends {

    public static final ECKey SIGNING_KEY = generateKey();

    /** Metadata of the last call received by any fake backend. */
    public final AtomicReference<Metadata> lastHeaders = new AtomicReference<>();

    // one server per Spring test context
    private final String serverName = InProcessServerBuilder.generateName();

    @Bean(destroyMethod = "shutdownNow")
    public Server fakeBackendServer() throws IOException {
        ServerInterceptor captureHeaders = new ServerInterceptor() {
            @Override
            public <Q, R> io.grpc.ServerCall.Listener<Q> interceptCall(io.grpc.ServerCall<Q, R> call, Metadata headers,
                                                                       io.grpc.ServerCallHandler<Q, R> next) {
                lastHeaders.set(headers);
                return next.startCall(call, headers);
            }
        };
        return InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(ServerInterceptors.intercept(new FakeProductService(), captureHeaders))
                .addService(ServerInterceptors.intercept(new FakeStockService(), captureHeaders))
                .addService(ServerInterceptors.intercept(new FakeUploadService(), captureHeaders))
                .addService(ServerInterceptors.intercept(new FakeReportService(), captureHeaders))
                .addService(new FakeAuthService())
                .build()
                .start();
    }

    @Bean
    @Primary
    public ChannelProvider fakeChannelProvider(Server fakeBackendServer) {
        var channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        return backend -> channel;
    }

    private static ECKey generateKey() {
        try {
            return new ECKeyGenerator(Curve.P_256).keyID("test-key").generate();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static class FakeProductService extends ProductServiceGrpc.ProductServiceImplBase {
        @Override
        public void getProduct(GetProductRequest request, StreamObserver<Product> responseObserver) {
            if (request.getCode().equals("missing")) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Product missing not found").asRuntimeException());
                return;
            }
            responseObserver.onNext(Product.newBuilder().setCode(request.getCode()).addNames("Green tea").build());
            responseObserver.onCompleted();
        }
    }

    static class FakeStockService extends StockServiceGrpc.StockServiceImplBase {
        @Override
        public void export(SearchStockRequest request, StreamObserver<StockLot> responseObserver) {
            responseObserver.onNext(StockLot.newBuilder().setId("lot-1").setProductCode(request.getProductCode()).build());
            if (request.getProductCode().equals("broken")) {
                responseObserver.onError(Status.INTERNAL.withDescription("disk on fire").asRuntimeException());
                return;
            }
            responseObserver.onNext(StockLot.newBuilder().setId("lot-2").setProductCode(request.getProductCode()).build());
            responseObserver.onCompleted();
        }
    }

    static class FakeUploadService extends UploadServiceGrpc.UploadServiceImplBase {
        @Override
        public StreamObserver<UploadChunk> upload(StreamObserver<UploadResult> responseObserver) {
            return new StreamObserver<>() {
                private String fileKind = "";
                private int bytes;

                @Override
                public void onNext(UploadChunk chunk) {
                    if (chunk.hasHeader()) {
                        fileKind = chunk.getHeader().getFileKind();
                    } else {
                        bytes += chunk.getData().size();
                    }
                }

                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(UploadResult.newBuilder().setUploadId(fileKind).setAcceptedRows(bytes).build());
                    responseObserver.onCompleted();
                }
            };
        }
    }

    static class FakeReportService extends ReportServiceGrpc.ReportServiceImplBase {
        @Override
        public void render(RenderRequest request, StreamObserver<FileChunk> responseObserver) {
            responseObserver.onNext(FileChunk.newBuilder()
                    .setFileName("stock.csv").setContentType("text/csv")
                    .setData(ByteString.copyFromUtf8("code,qty\n")).build());
            responseObserver.onNext(FileChunk.newBuilder().setData(ByteString.copyFromUtf8("P001,12\n")).build());
            responseObserver.onCompleted();
        }
    }

    static class FakeAuthService extends AuthServiceGrpc.AuthServiceImplBase {
        @Override
        public void getJwks(Empty request, StreamObserver<JwksResponse> responseObserver) {
            var jwks = new JWKSet(SIGNING_KEY.toPublicJWK()).toString();
            responseObserver.onNext(JwksResponse.newBuilder().setJwksJson(jwks).build());
            responseObserver.onCompleted();
        }
    }
}
