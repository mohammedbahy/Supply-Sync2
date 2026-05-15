package com.supplysync.repository;

import com.supplysync.models.Product;

import java.util.List;

/** Product / inventory persistence (ISP). */
public interface ProductRepository {
    void saveProduct(Product product);

    void deleteProduct(String productId);

    List<Product> findAllProducts();
}
