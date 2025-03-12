package com.example.electrical_preorder_system_backend.repository.specification;

import com.example.electrical_preorder_system_backend.entity.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderSpecification {

    public static Specification<Order> isDeleted(boolean isDeleted) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isDeleted"), isDeleted);
    }

    public static Specification<Order> hasStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Order> hasCreatedBetween(LocalDateTime min, LocalDateTime max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return criteriaBuilder.between(root.get("createdAt"), min, max);
            return min != null ? criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), min)
                    : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), max);
        };
    }

    public static Specification<Order> hasCampaignId(UUID campaignId) {
        if (campaignId == null) return null;
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("campaign").get("id"), campaignId);
    }

    public static Specification<Order> hasUserId(UUID userId) {
        if (userId == null) return null;
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Order> hasExpectedDeliveryDateBetween(LocalDateTime min, LocalDateTime max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return criteriaBuilder.between(root.get("expectedDeliveryDate"), min, max);
            return min != null ? criteriaBuilder.greaterThanOrEqualTo(root.get("expectedDeliveryDate"), min)
                    : criteriaBuilder.lessThanOrEqualTo(root.get("expectedDeliveryDate"), max);
        };
    }
}
