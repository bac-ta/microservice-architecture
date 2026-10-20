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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(ProductServiceGrpc.class)
public class ProductGrpcServer extends ProductServiceGrpc.ProductServiceImplBase {

    /**
     * Legacy: gwh_mpprd_010 / gwh_mbprd (HHT) / gwh_cpprd_010 (popup)
     */
    @Override
    public void getProduct(GetProductRequest request, StreamObserver<Product> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/GetProduct", "gwh_mpprd_010 / gwh_mbprd (HHT) / gwh_cpprd_010 (popup)"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void listProducts(ListProductsRequest request, StreamObserver<ListProductsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/ListProducts", "new in EPL"));
    }

    /**
     * Legacy: gwh_mpprd_010 register, upload via integration (lpupl FILE_KND 03)
     */
    @Override
    public void upsertProduct(Product request, StreamObserver<Product> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/UpsertProduct", "gwh_mpprd_010 register, upload via integration (lpupl FILE_KND 03)"));
    }

    /**
     * Legacy: gwh_cppbar (GWH_TM_PBAR) — HHT scans a barcode and needs the product
     */
    @Override
    public void resolveBarcode(ResolveBarcodeRequest request, StreamObserver<ResolveBarcodeResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ProductService/ResolveBarcode", "gwh_cppbar (GWH_TM_PBAR) — HHT scans a barcode and needs the product"));
    }
}
