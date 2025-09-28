package com.gm2dev.demo_spring.integration.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gm2dev.demo_spring.dto.order.CreateOrderRequest;
import com.gm2dev.demo_spring.dto.user.LoginRequest;
import com.gm2dev.demo_spring.entity.product.Product;
import com.gm2dev.demo_spring.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private String adminToken;
    private String userToken;
    private Product testProduct;

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

        // Create a test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void createOrder_ValidRequest_Success() throws Exception {
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.shippingAddress").value("123 Test Street, Test City, 12345"))
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2));
    }

    @Test
    void createOrder_InsufficientStock_BadRequest() throws Exception {
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(20); // More than available stock

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_NoAuth_Unauthorized() throws Exception {
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyOrders_ValidUser_Success() throws Exception {
        // First create an order
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then get user's orders
        mockMvc.perform(get("/orders/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getMyOrdersByStatus_ValidStatus_Success() throws Exception {
        // First create an order
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Get orders by status
        mockMvc.perform(get("/orders/my")
                        .header("Authorization", "Bearer " + userToken)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getAllOrders_WithAdminAuth_Success() throws Exception {
        // First create an order
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Admin gets all orders
        mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllOrders_WithUserAuth_Forbidden() throws Exception {
        mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderStatus_WithAdminAuth_Success() throws Exception {
        // First create an order
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        MvcResult createResult = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        // Admin updates order status
        mockMvc.perform(put("/orders/{orderId}/status", orderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelOrder_ValidOrder_Success() throws Exception {
        // First create an order
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        MvcResult createResult = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        // User cancels their order
        mockMvc.perform(put("/orders/my/{orderId}/cancel", orderId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getOrderById_WithAdminAuth_Success() throws Exception {
        // First create an order
        CreateOrderRequest.OrderItemRequest orderItem = new CreateOrderRequest.OrderItemRequest();
        orderItem.setProductId(testProduct.getId());
        orderItem.setQuantity(1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Test Street, Test City, 12345");
        request.setOrderItems(List.of(orderItem));

        MvcResult createResult = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

        // Admin gets specific order
        mockMvc.perform(get("/orders/{orderId}", orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }
}