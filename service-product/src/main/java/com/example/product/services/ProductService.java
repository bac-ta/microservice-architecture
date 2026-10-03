package com.example.product.services;

import com.example.core.autogen.grpc.product.DepartmentIdRequest;
import com.example.core.autogen.grpc.product.Product;
import com.example.core.autogen.grpc.product.ProductIdRequest;
import com.example.core.autogen.grpc.product.ProductResponses;
import com.example.core.autogen.grpc.product.ProductSaving;

public interface ProductService {

    Product findById(ProductIdRequest request);

    ProductResponses findByDepartmentId(DepartmentIdRequest request);

    ProductResponses findProducts();

    void saveProduct(ProductSaving product);

    void removeProductById(ProductIdRequest request);
}
