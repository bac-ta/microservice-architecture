package com.example.product.repositories;

import com.example.product.models.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product findById(int id);

    List<Product> findAllByDepartmentId(int departmentId);

    Product save(Product productEntity);

    void removeById(int productId);

    @Query(value = "SELECT * FROM product", nativeQuery = true)
    List<Product> findAll();
}
