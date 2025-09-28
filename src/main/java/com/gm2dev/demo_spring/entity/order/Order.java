package com.gm2dev.demo_spring.entity.order;

import com.gm2dev.demo_spring.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// JPA annotation that marks this class as a database entity
// Tells Hibernate/JPA that this class represents a table in the database
@Entity
// Specifies the actual database table name as "orders"
// Without this, JPA would use the class name "Order" which conflicts with SQL reserved word
@Table(name = "orders")
// Enables automatic auditing of entity lifecycle events
// Works with @CreatedDate and @LastModifiedDate to automatically set timestamps
@EntityListeners(AuditingEntityListener.class)
// Lombok annotation that auto-generates getters, setters, toString, equals, and hashCode methods
// Reduces boilerplate code significantly
@Data
// Lombok annotation that generates a no-argument constructor
// Required by JPA for entity instantiation
@NoArgsConstructor
// Lombok annotation that generates a constructor with all fields as parameters
// Useful for creating fully populated entities in tests
@AllArgsConstructor
// Lombok annotation that enables the Builder pattern for object creation
// Allows fluent, readable object construction: Order.builder().user(user).status(status).build()
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false)
    private String shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }


    public BigDecimal calculateTotal() {
        return orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateTotalAmount() {
        this.totalAmount = calculateTotal();
    }
}