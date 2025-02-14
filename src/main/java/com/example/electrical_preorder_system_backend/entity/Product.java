package com.example.electrical_preorder_system_backend.entity;

import com.example.electrical_preorder_system_backend.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Default;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Entity
@DynamicUpdate
@Table(name = "product", indexes = {
        @Index(name = "idx_product_code", columnList = "product_code"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category_id", columnList = "category_id"),
        @Index(name = "idx_product_slug", columnList = "slug")
})
@NoArgsConstructor
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    @Min(0)
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Min(0)
    private BigDecimal price;

    @Column
    @Min(0)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImageProduct> imageProducts = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Product(String productCode, String name, Integer quantity, String description, BigDecimal price, Integer position, Category category) {
        this.productCode = productCode;
        this.name = name;
        this.quantity = quantity;
        this.description = description;
        this.price = price;
        this.position = position;
        this.category = category;
    }
}
