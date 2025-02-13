package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByProductCode(String productCode);

    boolean existsBySlug(String slug);

    Product findByProductCode(String productCode);

    @Query(value = "SELECT * FROM products " +
            "WHERE is_deleted = false " +
            "ORDER BY " +
            "CASE WHEN position = 0 THEN 1 ELSE 0 END, " +
            "position ASC, " +
            "created_at ASC",
            countQuery = "SELECT count(*) FROM products WHERE is_deleted = false",
            nativeQuery = true)
    Page<Product> findActiveProductsSorted(Pageable pageable);

    @Query(value = "SELECT * FROM products " +
            "WHERE is_deleted = false AND category_id IN (SELECT id FROM categories WHERE name = ?1) " +
            "ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC, created_at ASC",
            countQuery = "SELECT count(*) FROM products WHERE is_deleted = false AND category_id IN (SELECT id FROM categories WHERE name = ?1)",
            nativeQuery = true)
    Page<Product> findByCategory_NameAndIsDeletedFalse(String categoryName, Pageable pageable);

    @Query(value = "SELECT * FROM products " +
            "WHERE is_deleted = false AND LOWER(name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "ORDER BY CASE WHEN position = 0 THEN 1 ELSE 0 END, position ASC, created_at ASC",
            countQuery = "SELECT count(*) FROM products WHERE is_deleted = false AND LOWER(name) LIKE LOWER(CONCAT('%', ?1, '%'))",
            nativeQuery = true)
    Page<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);
}
