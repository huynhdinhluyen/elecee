package com.example.electrical_preorder_system_backend.repository.specification;

import com.example.electrical_preorder_system_backend.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class UserSpecification {
    public static Specification<User> isDeleted(boolean isDeleted) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isDeleted"), isDeleted);
    }

    public static Specification<User> isVerified(boolean isVerified) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isVerified"), isVerified);
    }
    public static Specification<User> createdAtBetween(LocalDateTime min, LocalDateTime max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return criteriaBuilder.between(root.get("createdAt"), min, max);
            return min != null ? criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), min)
                    : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), max);
        };
    }

    public static Specification<User> search(String search) {
        if (search == null || search.isBlank()) return null;
        String likePattern = "%" + search.toLowerCase() + "%";
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), likePattern)
        );
    }

    public static Specification<User> hasRole(String role) {

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<User> hasNonExpiredToken() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("tokenExpires"), LocalDateTime.now(ZoneId.systemDefault()));
    }

}
