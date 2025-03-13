package com.example.electrical_preorder_system_backend.service.payment;

import com.example.electrical_preorder_system_backend.dto.request.payment.CreatePaymentRequest;
import com.example.electrical_preorder_system_backend.dto.request.payment.PaymentPayload;
import com.example.electrical_preorder_system_backend.dto.response.PaymentDTO;
import com.example.electrical_preorder_system_backend.entity.Payment;
import com.example.electrical_preorder_system_backend.entity.User;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;

import java.util.List;
import java.util.Map;

public interface IPaymentService {

    CheckoutResponseData createPaymentLink(User user, List<ItemData> items , Integer amount, Payment payment, int retryCreatePayment);

    CheckoutResponseData createPaymentLink(User user, CreatePaymentRequest createPaymentRequest);

    Map<String, Boolean> handleWebhook(PaymentPayload payload);

    PaymentDTO get(Long paymentId);

    PaymentDTO getPaymentLinkInformation(Long paymentId);
}
