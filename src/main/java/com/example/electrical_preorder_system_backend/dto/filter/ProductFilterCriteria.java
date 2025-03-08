package com.example.electrical_preorder_system_backend.dto.filter;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterCriteria {
    private String category;
    private String query;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
