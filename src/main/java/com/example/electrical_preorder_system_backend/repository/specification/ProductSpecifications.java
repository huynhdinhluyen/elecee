package com.example.electrical_preorder_system_backend.repository.specification;

import com.example.electrical_preorder_system_backend.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {
    public static Specification<Product> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category").get("name")), category.toLowerCase());
    }

    public static Specification<Product> matchesQuery(String query) {
        String likePattern = "%" + query.toLowerCase() + "%";
        return (root, criteriaQuery, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), likePattern),
                cb.like(cb.lower(root.get("productCode")), likePattern),
                cb.like(cb.lower(root.get("description")), likePattern)
        );
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal price) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), price);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal price) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), price);
    }
}
