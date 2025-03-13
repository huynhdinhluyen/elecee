package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    boolean existsByProductCode(String productCode);

    boolean existsBySlug(String slug);

    @Query(value = "SELECT * " +
            "FROM product " +
            "WHERE is_deleted = false " +
            "ORDER BY " +
            "CASE WHEN position = 0 THEN 1 ELSE 0 END, " +
            "position ASC, " +
            "created_at ASC",
            countQuery = "SELECT count(*) " +
                    "FROM product " +
                    "WHERE is_deleted = false",
            nativeQuery = true)
    Page<Product> findActiveProductsSorted(Pageable pageable);

    @Query(value = "SELECT COUNT(*) " +
            "FROM product " +
            "WHERE is_deleted = false",
            nativeQuery = true)
    Long countActiveProducts();

    @Query(value = "SELECT * " +
            "FROM product " +
            "WHERE is_deleted = false AND slug = ?1",
            nativeQuery = true)
    Product findBySlug(String slug);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isDeleted = false")
    long countByCategoryIdAndIsDeletedFalse(@Param("categoryId") UUID categoryId);
}