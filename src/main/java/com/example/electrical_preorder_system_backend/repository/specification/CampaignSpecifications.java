package com.example.electrical_preorder_system_backend.repository.specification;

import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class CampaignSpecifications {

    public static Specification<Campaign> isNotDeleted() {
        return (root, query, cb) ->
                cb.equal(root.get("isDeleted"), false);
    }

    public static Specification<Campaign> nameLike(String name) {
        return (root, query, cb) ->
                name == null || name.trim().isEmpty() ? cb.conjunction() :
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Campaign> hasStatus(CampaignStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() :
                        cb.equal(root.get("status"), status);
    }

    public static Specification<Campaign> hasProductId(UUID productId) {
        return (root, query, cb) ->
                productId == null ? cb.conjunction() :
                        cb.equal(root.get("product").get("id"), productId);
    }

    public static Specification<Campaign> startDateAfterOrEqual(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("startDate"), date);
    }

    public static Specification<Campaign> startDateBeforeOrEqual(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() :
                        cb.lessThanOrEqualTo(root.get("startDate"), date);
    }

    public static Specification<Campaign> endDateAfterOrEqual(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("endDate"), date);
    }

    public static Specification<Campaign> endDateBeforeOrEqual(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? cb.conjunction() :
                        cb.lessThanOrEqualTo(root.get("endDate"), date);
    }
}