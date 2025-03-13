package com.example.electrical_preorder_system_backend.dto.request.payment;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentPayload {
    private String code;
    private String desc;
    private boolean success;
    private PaymentPayloadData data;
    private String signature;
}
