package com.epl.masterdata.grpc;

import com.epl.masterdata.services.ProductService;
import com.epl.masterdata.v1.DeleteProductRequest;
import com.epl.masterdata.v1.GetProductRequest;
import com.epl.masterdata.v1.ListProductsRequest;
import com.epl.masterdata.v1.ListProductsResponse;
import com.epl.masterdata.v1.Product;
import com.epl.masterdata.v1.ProductServiceGrpc;
import com.epl.masterdata.v1.ResolveBarcodeRequest;
import com.epl.masterdata.v1.ResolveBarcodeResponse;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/**
 * gRPC endpoint epl.masterdata.v1.ProductService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Products of the tenant in x-company / x-warehouse / x-customer.
 */
@RequiredArgsConstructor
@GrpcServer(ProductServiceGrpc.class)
public class ProductGrpcServer extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<Product> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> productService.get(request.getCode()));
    }

    @Override
    public void listProducts(ListProductsRequest request, StreamObserver<ListProductsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> productService.list(request));
    }

    @Override
    public void upsertProduct(Product request, StreamObserver<Product> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> productService.upsert(request));
    }

    @Override
    public void deleteProduct(DeleteProductRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> productService.delete(request.getCode(), request.getVersion()));
    }

    @Override
    public void resolveBarcode(ResolveBarcodeRequest request, StreamObserver<ResolveBarcodeResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> productService.resolveBarcode(request.getBarcode()));
    }
}
