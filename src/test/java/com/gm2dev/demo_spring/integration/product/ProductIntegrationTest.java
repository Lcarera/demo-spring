package com.gm2dev.demo_spring.integration.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gm2dev.demo_spring.dto.product.CreateProductRequest;
import com.gm2dev.demo_spring.dto.product.UpdateProductRequest;
import com.gm2dev.demo_spring.dto.user.LoginRequest;
import com.gm2dev.demo_spring.entity.product.Product;
import com.gm2dev.demo_spring.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        // Get admin token
        LoginRequest adminLogin = new LoginRequest("admin", "admin123");
        MvcResult adminResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminResponse = adminResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(adminResponse).get("accessToken").asText();

        // Get user token
        LoginRequest userLogin = new LoginRequest("user", "user123");
        MvcResult userResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String userResponse = userResult.getResponse().getContentAsString();
        userToken = objectMapper.readTree(userResponse).get("accessToken").asText();
    }

    @Test
        void getAllProducts_NoAuth_Success() throws Exception {
        // Create a test product
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();
        productRepository.save(product);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void getProductById_ValidId_Success() throws Exception {
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();
        Product savedProduct = productRepository.save(product);

        mockMvc.perform(get("/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    void getProductById_InvalidId_NotFound() throws Exception {
        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_WithAdminAuth_Success() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "New Product",
                "New Description",
                new BigDecimal("149.99"),
                5,
                "Books",
                "http://example.com/image.jpg"
        );

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(149.99));
    }

    @Test
    void createProduct_WithUserAuth_Forbidden() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "New Product",
                "New Description",
                new BigDecimal("149.99"),
                5,
                "Books",
                null
        );

        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_NoAuth_Unauthorized() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "New Product",
                "New Description",
                new BigDecimal("149.99"),
                5,
                "Books",
                null
        );

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProduct_WithAdminAuth_Success() throws Exception {
        Product product = Product.builder()
                .name("Original Product")
                .description("Original Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();
        Product savedProduct = productRepository.save(product);

        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Product",
                "Updated Description",
                new BigDecimal("199.99"),
                20,
                "Electronics",
                null
        );

        mockMvc.perform(put("/products/{id}", savedProduct.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(199.99));
    }

    @Test
    void deleteProduct_WithAdminAuth_Success() throws Exception {
        Product product = Product.builder()
                .name("Product to Delete")
                .description("Will be deleted")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();
        Product savedProduct = productRepository.save(product);

        mockMvc.perform(delete("/products/{id}", savedProduct.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify product is deleted
        mockMvc.perform(get("/products/{id}", savedProduct.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductsByCategory_ValidCategory_Success() throws Exception {
        Product product1 = Product.builder()
                .name("Electronics Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();

        Product product2 = Product.builder()
                .name("Books Product")
                .description("Test Description")
                .price(new BigDecimal("29.99"))
                .stockQuantity(5)
                .category("Books")
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        mockMvc.perform(get("/products")
                        .param("category", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].category").value("Electronics"));
    }

    @Test
    void searchProducts_ValidQuery_Success() throws Exception {
        Product product = Product.builder()
                .name("Searchable Product")
                .description("This product should be found")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();
        productRepository.save(product);

        mockMvc.perform(get("/products")
                        .param("search", "Searchable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Searchable Product"));
    }

    @Test
    void getCategories_Success() throws Exception {
        Product product1 = Product.builder()
                .name("Electronics Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();

        Product product2 = Product.builder()
                .name("Books Product")
                .description("Test Description")
                .price(new BigDecimal("29.99"))
                .stockQuantity(5)
                .category("Books")
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        mockMvc.perform(get("/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").isString());
    }
}