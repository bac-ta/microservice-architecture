package com.example.epl.masterdata.grpc;

import com.example.epl.masterdata.v1.GetProductRequest;
import com.example.epl.masterdata.v1.ListProductsRequest;
import com.example.epl.masterdata.v1.ListProductsResponse;
import com.example.epl.masterdata.v1.Product;
import com.example.epl.masterdata.v1.ProductServiceGrpc;
import com.example.epl.masterdata.v1.ResolveBarcodeRequest;
import com.example.epl.masterdata.v1.ResolveBarcodeResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.masterdata.v1.ProductService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(ProductServiceGrpc.class)
public class ProductGrpcServer extends ProductServiceGrpc.ProductServiceImplBase {

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<Product> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/GetProduct", "EPL-MD-210W", "EPL-MD-210H"));
    }

    @Override
    public void listProducts(ListProductsRequest request, StreamObserver<ListProductsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/ListProducts", "EPL-MD-210W"));
    }

    @Override
    public void upsertProduct(Product request, StreamObserver<Product> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/UpsertProduct", "EPL-MD-210W"));
    }

    @Override
    public void resolveBarcode(ResolveBarcodeRequest request, StreamObserver<ResolveBarcodeResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/ResolveBarcode"));
    }
}
