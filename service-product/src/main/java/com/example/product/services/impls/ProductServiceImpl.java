package com.example.product.services.impls;

import com.example.core.autogen.grpc.product.DepartmentIdRequest;
import com.example.core.autogen.grpc.product.ProductIdRequest;
import com.example.core.autogen.grpc.product.ProductResponses;
import com.example.core.autogen.grpc.product.ProductSaving;
import com.example.product.models.entities.Product;
import com.example.product.repositories.ProductRepository;
import com.example.product.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public com.example.core.autogen.grpc.product.Product findById(ProductIdRequest request) {
        int productId = request.getId();
        logger.info("Product Id: " + productId);
        var entity = productRepository.findById(productId);

        return entity.initProduct(entity);
    }

    public ProductResponses findByDepartmentId(DepartmentIdRequest request) {
        int departmentId = request.getDepartmentId();
        logger.info("Department Id: " + departmentId);
        var entities = productRepository.findAllByDepartmentId(departmentId);
        var products = entities.stream().map(entity -> entity.initProduct(entity)).collect(Collectors.toList());

        return ProductResponses.newBuilder().addAllProduct(products).build();
    }

    public ProductResponses findProducts() {
        var entities = productRepository.findAll();
        var products = entities.stream().map(entity -> entity.initProduct(entity)).collect(Collectors.toList());

        return ProductResponses.newBuilder().addAllProduct(products).build();
    }

    public void saveProduct(ProductSaving product) {
        var entity = new Product(product);
        try {
            productRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeProductById(ProductIdRequest request) {
        int id = request.getId();
        logger.info("Product Id: " + id);
        productRepository.removeById(id);
    }
}
