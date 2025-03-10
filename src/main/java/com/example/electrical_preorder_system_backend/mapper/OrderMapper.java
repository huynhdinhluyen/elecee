package com.example.electrical_preorder_system_backend.mapper;

import com.example.electrical_preorder_system_backend.dto.response.OrderDTO;
import com.example.electrical_preorder_system_backend.dto.response.OrderListDTO;
import com.example.electrical_preorder_system_backend.entity.Order;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .quantity(order.getQuantity())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .user(UserMapper.toUserDTO(order.getUser()))
                .campaign(CampaignMapper.toCampaignDTO(order.getCampaign()))
                .isDeleted(order.isDeleted())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderListDTO toOrderListDTO(List<Order> orders, int totalPages, long totalElements, int currentPage, int pageSize) {
        return OrderListDTO.builder()
                .orders(orders.stream().map(OrderMapper::toOrderDTO).collect(Collectors.toList()))
                .totalPages(totalPages)
                .totalElements(totalElements)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .build();
    }
}
