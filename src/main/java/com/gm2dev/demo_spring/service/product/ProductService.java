package com.gm2dev.demo_spring.service.product;

import com.gm2dev.demo_spring.entity.product.Product;
import com.gm2dev.demo_spring.exception.ResourceNotFoundException;
import com.gm2dev.demo_spring.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProductsByCategory(String category, String name, Pageable pageable) {
        return productRepository.findByCategoryAndNameContainingIgnoreCase(category, name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getInStockProducts(Pageable pageable) {
        return productRepository.findAllInStock(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getInStockProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryAndInStock(category, pageable);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public Boolean isProductNameAvailable(String name) {
        return !productRepository.existsByName(name);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Product createProduct(Product product) {
        log.info("Creating product: {}", product.getName());
        return productRepository.save(product);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(Long productId, Product productDetails) {
        Product product = getProductById(productId);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());

        log.info("Updating product: {}", product.getName());
        return productRepository.save(product);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long productId) {
        Product product = getProductById(productId);
        log.info("Deleting product: {}", product.getName());
        productRepository.delete(product);
    }

    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }
}