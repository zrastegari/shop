package com.learning.shop.service;

import com.learning.shop.model.Product;
import com.learning.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("محصول با id " + id + " یافت نشد"));
    }

    @Override
    public Product addProduct(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }
}