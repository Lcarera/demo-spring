package com.gm2dev.demo_spring.controller.product;

import com.gm2dev.demo_spring.dto.product.CreateProductRequest;
import com.gm2dev.demo_spring.dto.product.ProductResponse;
import com.gm2dev.demo_spring.dto.product.UpdateProductRequest;
import com.gm2dev.demo_spring.entity.product.Product;
import com.gm2dev.demo_spring.mapper.product.ProductMapper;
import com.gm2dev.demo_spring.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Product catalog and management APIs")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    @Operation(summary = "Get all products", description = "Get paginated list of all products with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products")
    })
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Name search filter") @RequestParam(required = false) String search,
            @Parameter(description = "Show only in-stock products") @RequestParam(defaultValue = "false") Boolean inStockOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Product> products;

        if (inStockOnly) {
            if (category != null) {
                products = productService.getInStockProductsByCategory(category, pageable);
            } else {
                products = productService.getInStockProducts(pageable);
            }
        } else if (category != null && search != null) {
            products = productService.searchProductsByCategory(category, search, pageable);
        } else if (search != null) {
            products = productService.searchProducts(search, pageable);
        } else if (category != null) {
            products = productService.getProductsByCategory(category, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        Page<ProductResponse> productResponses = products.map(productMapper::toProductResponse);
        return ResponseEntity.ok(productResponses);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Get detailed product information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(productMapper.toProductResponse(product));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Get list of all available product categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved categories")
    })
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/check-name")
    @Operation(summary = "Check product name availability", description = "Check if product name is available")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Name availability checked")
    })
    public ResponseEntity<Boolean> checkProductNameAvailability(
            @Parameter(description = "Product name to check") @RequestParam String name) {
        Boolean isAvailable = productService.isProductNameAvailable(name);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create new product", description = "Create a new product (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Product creation data") @Valid @RequestBody CreateProductRequest createProductRequest) {
        Product product = productMapper.toProduct(createProductRequest);
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productMapper.toProductResponse(createdProduct));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product", description = "Update an existing product (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Product update data") @Valid @RequestBody UpdateProductRequest updateProductRequest) {

        Product existingProduct = productService.getProductById(productId);
        productMapper.updateProductFromRequest(updateProductRequest, existingProduct);
        Product updatedProduct = productService.updateProduct(productId, existingProduct);
        return ResponseEntity.ok(productMapper.toProductResponse(updatedProduct));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete product", description = "Delete a product (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}