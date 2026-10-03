package com.example.product.configurations;

import com.example.core.autogen.grpc.product.DepartmentIdRequest;
import com.example.core.autogen.grpc.product.Product;
import com.example.core.autogen.grpc.product.ProductIdRequest;
import com.example.core.autogen.grpc.product.ProductResponses;
import com.example.core.autogen.grpc.product.ProductSaving;
import com.example.core.autogen.grpc.product.ProductServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.product.services.ProductService;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcServer(value = ProductServiceGrpc.class)
public class ProductGrpcServer extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService service;

    @Autowired
    public ProductGrpcServer(final ProductService service) {
        this.service = service;
    }

    @Override
    public void findProductById(ProductIdRequest request, StreamObserver<Product> responseObserver) {
        responseObserver.onNext(service.findById(request));
        responseObserver.onCompleted();
    }

    @Override
    public void findProductByDepartmentId(DepartmentIdRequest request, StreamObserver<ProductResponses> responseObserver) {
        ProductResponses responses = service.findByDepartmentId(request);
        responseObserver.onNext(responses);
        responseObserver.onCompleted();
    }

    @Override
    public void findProducts(Empty request, StreamObserver<ProductResponses> responseObserver) {
        ProductResponses responses = service.findProducts();
        responseObserver.onNext(responses);
        responseObserver.onCompleted();
    }

    @Override
    public void removeProductById(ProductIdRequest request, StreamObserver<Empty> responseObserver) {
        service.removeProductById(request);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void saveProduct(ProductSaving request, StreamObserver<Empty> responseObserver) {
        service.saveProduct(request);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}
