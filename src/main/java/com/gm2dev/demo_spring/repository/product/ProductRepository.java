package com.gm2dev.demo_spring.repository.product;

import com.gm2dev.demo_spring.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryAndNameContainingIgnoreCase(String category, String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    Page<Product> findAllInStock(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.stockQuantity > 0")
    Page<Product> findByCategoryAndInStock(@Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();

    Boolean existsByName(String name);
}