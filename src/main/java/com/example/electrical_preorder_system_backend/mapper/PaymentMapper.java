package com.example.electrical_preorder_system_backend.mapper;

import com.example.electrical_preorder_system_backend.dto.response.PaymentDTO;
import com.example.electrical_preorder_system_backend.entity.Order;
import com.example.electrical_preorder_system_backend.entity.Payment;
import com.example.electrical_preorder_system_backend.entity.Product;
import vn.payos.type.ItemData;

import java.util.stream.Collectors;

public class PaymentMapper {

    public static ItemData toItemData(Order order, Product product) {
        return ItemData.builder()
                .name(product.getName())
                .quantity(order.getQuantity())
                .price(order.getTotalAmount().intValue())
                .build();
    }

    public static PaymentDTO toPaymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .date(payment.getDate())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .orderIds(payment.getOrders().stream().map(Order::getId).collect(Collectors.toList()))
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

}