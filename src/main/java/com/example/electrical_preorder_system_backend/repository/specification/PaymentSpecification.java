package com.example.electrical_preorder_system_backend.repository.specification;

import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.Order;
import com.example.electrical_preorder_system_backend.entity.Payment;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.enums.PaymentMethod;
import com.example.electrical_preorder_system_backend.enums.PaymentStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentSpecification {
    public static Specification<Payment> hasMethod(PaymentMethod method) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("method"), method);
    }

    public static Specification<Payment> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Payment, Order> orderJoin = root.join("orders", JoinType.INNER);
            return criteriaBuilder.equal(orderJoin.get("user").get("id"), userId);
        };
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Payment> hasProductId(UUID productId) {
        return (root, query, criteriaBuilder) -> {
            if (productId == null) {
                return criteriaBuilder.conjunction();
            }

            try {

                Join<Payment, Order> orderJoin = root.join("orders", JoinType.INNER);
                Join<Order, Campaign> campaignJoin = orderJoin.join("campaign", JoinType.INNER);
                Join<Campaign, Product> productJoin = campaignJoin.join("product", JoinType.INNER);

                // Use distinct to prevent duplicate results
                assert query != null;
                query.distinct(true);

                return criteriaBuilder.equal(productJoin.get("id"), productId);
            } catch (IllegalArgumentException e) {
                // Handle invalid UUID format
                return criteriaBuilder.disjunction();
            }
        };
    }

    public static Specification<Payment> createdAtFrom(LocalDateTime createdAtFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom);
    }

    public static Specification<Payment> createdAtTo(LocalDateTime createdAtTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdAtTo);
    }

    public static Specification<Payment> hasAmountGreaterThan(BigDecimal min) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Payment> hasAmountLessThan(BigDecimal max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("amount"), max);
    }

}
