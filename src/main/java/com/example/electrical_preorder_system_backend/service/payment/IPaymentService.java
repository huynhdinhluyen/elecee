package com.example.electrical_preorder_system_backend.service.payment;

import com.example.electrical_preorder_system_backend.dto.request.payment.CreatePaymentRequest;
import com.example.electrical_preorder_system_backend.dto.request.payment.PaymentPayload;
import com.example.electrical_preorder_system_backend.dto.response.payment.PaymentDTO;
import com.example.electrical_preorder_system_backend.dto.response.payment.PaymentListDTO;
import com.example.electrical_preorder_system_backend.entity.Payment;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.PaymentMethod;
import com.example.electrical_preorder_system_backend.enums.PaymentStatus;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IPaymentService {

    CheckoutResponseData createPaymentLink(User user, List<ItemData> items , Integer amount, Payment payment, int retryCreatePayment);

    CheckoutResponseData createPaymentLink(User user, CreatePaymentRequest createPaymentRequest);

    Map<String, Boolean> handleWebhook(PaymentPayload payload);

    PaymentDTO get(Long paymentId);

    PaymentDTO getPaymentLinkInformation(Long paymentId);

    PaymentListDTO getPayments(int page, int size, String sortDirection, String sortField, UUID productId,
                               BigDecimal amountFrom, BigDecimal amountTo, PaymentStatus status, PaymentMethod method,
                               LocalDateTime createdAtFrom, LocalDateTime createdAtTo, UUID userId) throws AccessDeniedException;
}
