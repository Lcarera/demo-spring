package com.gm2dev.demo_spring.service.order;

import com.gm2dev.demo_spring.entity.order.Order;
import com.gm2dev.demo_spring.entity.order.OrderItem;
import com.gm2dev.demo_spring.entity.order.OrderStatus;
import com.gm2dev.demo_spring.entity.product.Product;
import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.exception.ResourceNotFoundException;
import com.gm2dev.demo_spring.repository.order.OrderRepository;
import com.gm2dev.demo_spring.security.UserPrincipal;
import com.gm2dev.demo_spring.service.product.ProductService;
import com.gm2dev.demo_spring.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gm2dev.demo_spring.dto.order.CreateOrderRequest;
import com.gm2dev.demo_spring.dto.order.CreateOrderRequest.OrderItemRequest;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<Order> getUserOrders(UserPrincipal currentUser, Pageable pageable) {
        return orderRepository.findByUserIdWithItems(currentUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> getUserOrdersByStatus(UserPrincipal currentUser, OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, UserPrincipal currentUser) {
        return orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllWithItems(pageable);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Order getOrderByIdAdmin(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    @Transactional
    public Order createOrder(UserPrincipal currentUser, String shippingAddress, List<CreateOrderRequest.OrderItemRequest> orderItems) {
        User user = userService.getCurrentUser(currentUser);

        Order order = Order.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .status(OrderStatus.PENDING)
                .build();

        for (OrderItemRequest itemRequest : orderItems) {
            Product product = productService.getProductById(itemRequest.getProductId());

            // Check stock availability
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);

            // Decrease stock
            productService.decreaseStock(product.getId(), itemRequest.getQuantity());
        }

        order.updateTotalAmount();

        log.info("Creating order for user: {} with total: {}", user.getUsername(), order.getTotalAmount());
        return orderRepository.save(order);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderByIdAdmin(orderId);
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        log.info("Updating order {} status from {} to {}", orderId, oldStatus, newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId, UserPrincipal currentUser) {
        Order order = getOrderById(orderId, currentUser);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore stock for cancelled items
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }

        log.info("Cancelled order: {}", orderId);
        return orderRepository.save(order);
    }
}