package com.example.electrical_preorder_system_backend.dto.request;
import com.example.electrical_preorder_system_backend.enums.PaymentMethod;
import lombok.*;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CreatePaymentRequest {
    private List<UUID> orderIds = List.of();
    private String buyerName;
    private String buyerPhone;
    private String buyerAddress;
    private PaymentMethod method;
}
