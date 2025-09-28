package com.gm2dev.demo_spring.mapper.order;

import com.gm2dev.demo_spring.dto.order.OrderItemResponse;
import com.gm2dev.demo_spring.dto.order.OrderResponse;
import com.gm2dev.demo_spring.entity.order.Order;
import com.gm2dev.demo_spring.entity.order.OrderItem;
import com.gm2dev.demo_spring.mapper.product.ProductMapper;
import com.gm2dev.demo_spring.mapper.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ProductMapper.class})
public interface OrderMapper {

    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "subtotal", expression = "java(orderItem.getSubtotal())")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}