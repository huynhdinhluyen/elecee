package com.example.electrical_preorder_system_backend.dto.response.payment;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaymentListDTO {
    private List<PaymentDTO> payments;
    private long totalAmount = 0L;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
