package com.example.electrical_preorder_system_backend.dto.cache;

import com.example.electrical_preorder_system_backend.dto.response.product.ProductDTO;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CachedProductPage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductDTO> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;

    public static CachedProductPage from(Page<ProductDTO> page) {
        CachedProductPage cachedPage = new CachedProductPage();
        cachedPage.setContent(page.getContent());
        cachedPage.setNumber(page.getNumber());
        cachedPage.setSize(page.getSize());
        cachedPage.setTotalElements(page.getTotalElements());
        cachedPage.setTotalPages(page.getTotalPages());
        return cachedPage;
    }

    public Page<ProductDTO> toPage() {
        return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
    }
}