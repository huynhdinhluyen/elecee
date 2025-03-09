package com.example.electrical_preorder_system_backend.service.payment;

import com.example.electrical_preorder_system_backend.dto.request.CreatePaymentRequest;
import com.example.electrical_preorder_system_backend.dto.request.PaymentPayload;
import com.example.electrical_preorder_system_backend.dto.response.PaymentDTO;
import com.example.electrical_preorder_system_backend.entity.User;
import vn.payos.type.CheckoutResponseData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IPaymentService {

    CheckoutResponseData createPaymentLink(User user, CreatePaymentRequest request, Integer amount, Long paymentId);

    CheckoutResponseData createPaymentLink(User user, CreatePaymentRequest createPaymentRequest);

    Map<String, Boolean> handleWebhook(PaymentPayload payload);

    PaymentDTO get(Long paymentId);

    PaymentDTO getPaymentLinkInformation(Long paymentId);
}
