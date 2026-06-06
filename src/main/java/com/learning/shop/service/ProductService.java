package com.learning.shop.service;

import com.learning.shop.model.Category;
import com.learning.shop.model.Product;
import java.util.List;

public interface ProductService {

    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product addProduct(Product product);

    Product updateProduct(Long id, Product productDetails);

    void deleteProduct(Long id);

    List<Product> getProductsByCategory(Category category);

    List<Product> searchProductsByName(String keyword);
}