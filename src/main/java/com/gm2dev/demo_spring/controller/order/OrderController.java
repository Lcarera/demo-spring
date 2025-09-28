package com.gm2dev.demo_spring.controller.order;

import com.gm2dev.demo_spring.dto.order.CreateOrderRequest;
import com.gm2dev.demo_spring.dto.order.OrderResponse;
import com.gm2dev.demo_spring.entity.order.Order;
import com.gm2dev.demo_spring.entity.order.OrderStatus;
import com.gm2dev.demo_spring.mapper.order.OrderMapper;
import com.gm2dev.demo_spring.security.CurrentUser;
import com.gm2dev.demo_spring.security.UserPrincipal;
import com.gm2dev.demo_spring.service.order.OrderService;
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

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Order Management", description = "Order creation and management APIs")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping("/my")
    @Operation(summary = "Get my orders", description = "Get paginated list of current user's orders")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @Parameter(description = "Filter by order status") @RequestParam(required = false) OrderStatus status,
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Order> orders;
        if (status != null) {
            orders = orderService.getUserOrdersByStatus(currentUser, status, pageable);
        } else {
            orders = orderService.getUserOrders(currentUser, pageable);
        }

        Page<OrderResponse> orderResponses = orders.map(orderMapper::toOrderResponse);
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/my/{orderId}")
    @Operation(summary = "Get my order by ID", description = "Get detailed information about a specific order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getMyOrderById(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @CurrentUser UserPrincipal currentUser) {
        Order order = orderService.getOrderById(orderId, currentUser);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping
    @Operation(summary = "Create new order", description = "Create a new order with items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or insufficient stock"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Order creation data") @Valid @RequestBody CreateOrderRequest createOrderRequest,
            @CurrentUser UserPrincipal currentUser) {
        Order order = orderService.createOrder(
                currentUser,
                createOrderRequest.getShippingAddress(),
                createOrderRequest.getOrderItems()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toOrderResponse(order));
    }

    @PutMapping("/my/{orderId}/cancel")
    @Operation(summary = "Cancel my order", description = "Cancel a pending order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Order cannot be cancelled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> cancelMyOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @CurrentUser UserPrincipal currentUser) {
        Order order = orderService.cancelOrder(orderId, currentUser);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders", description = "Get paginated list of all orders (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @Parameter(description = "Filter by order status") @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Order> orders;
        if (status != null) {
            orders = orderService.getOrdersByStatus(status, pageable);
        } else {
            orders = orderService.getAllOrders(pageable);
        }

        Page<OrderResponse> orderResponses = orders.map(orderMapper::toOrderResponse);
        return ResponseEntity.ok(orderResponses);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get order by ID", description = "Get detailed order information by ID (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved order"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        Order order = orderService.getOrderByIdAdmin(orderId);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status", description = "Update the status of an order (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "New order status") @RequestParam OrderStatus status) {
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }
}