package com.example.electrical_preorder_system_backend.service.payment;

import com.example.electrical_preorder_system_backend.dto.request.CreatePaymentRequest;
import com.example.electrical_preorder_system_backend.dto.request.PaymentPayload;
import com.example.electrical_preorder_system_backend.dto.response.PaymentDTO;
import com.example.electrical_preorder_system_backend.entity.Order;
import com.example.electrical_preorder_system_backend.entity.Payment;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.OrderStatus;
import com.example.electrical_preorder_system_backend.enums.PaymentStatus;
import com.example.electrical_preorder_system_backend.mapper.PaymentMapper;
import com.example.electrical_preorder_system_backend.repository.OrderRepository;
import com.example.electrical_preorder_system_backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    @Value("${payos.payment.return-url}")
    private String DEFAULT_PAYMENT_RETURN_URL;

    @Value("${payos.payment.cancel-url}")
    private String DEFAULT_PAYMENT_CANCEL_URL;

    @Value("${payos.payment.expire-time}")
    private Long DEFAULT_PAYMENT_EXPIRE_TIME;

    private final PayOS payOS;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public CheckoutResponseData createPaymentLink(User user, CreatePaymentRequest request, Integer amount, Long paymentId) {

        //Create Items
        List<ItemData> items = new ArrayList<>();
        for (UUID orderId : request.getOrderIds()) {
            Order order = orderRepository.findById(orderId).orElseThrow(
                    () -> new RuntimeException("Order " + orderId + " not found")
            );
            items.add(PaymentMapper.toItemData(order, order.getCampaign().getProduct()));
        }

        long currentSeconds = (int) (System.currentTimeMillis()/1000);

        PaymentData paymentData = PaymentData.builder()
                    .orderCode(paymentId)
                    .amount(amount)
                    .buyerEmail(user.getEmail())
                    .description(UUID.randomUUID().toString().substring(0, 24))
                    .items(items)
                    .returnUrl(DEFAULT_PAYMENT_RETURN_URL)
                    .cancelUrl(DEFAULT_PAYMENT_CANCEL_URL)
                    .expiredAt(currentSeconds + DEFAULT_PAYMENT_EXPIRE_TIME)
                    .build();
        try{
            return payOS.createPaymentLink(paymentData);
        }catch (Exception e){
            log.info("Error while creating payment link:", e);
            throw new RuntimeException("Error while creating payment link");
        }
    }

    @Override
    public CheckoutResponseData createPaymentLink(User user, CreatePaymentRequest createPaymentRequest) {
        try {
            Payment payment = new Payment();
            payment.setMethod(createPaymentRequest.getMethod());
            payment.setStatus(PaymentStatus.PENDING);
            List<Order> orders = orderRepository.findAllById(createPaymentRequest.getOrderIds());
            for (Order order : orders) {
                if (order.getStatus().equals(OrderStatus.PENDING)) {//Only pending orders can be paid
                    payment.getOrders().add(order);
                    payment.setAmount(payment.getAmount().add(order.getTotalAmount()));
                }else {
                    throw new RuntimeException("Order " + order.getId() + " is not pending");
                }
            }
            Payment savedPayment = paymentRepository.save(payment);
            return createPaymentLink(user, createPaymentRequest, payment.getAmount().intValue(), savedPayment.getId());
        }catch (Exception e){
            log.info("Error while creating payment:", e);
            throw new RuntimeException("Error while creating payment");
        }
    }

    @Override
    public Map<String, Boolean> handleWebhook(PaymentPayload payload) {
        try {
            Payment payment = paymentRepository.getReferenceById(payload.getData().getOrderCode());
            if (payment.getStatus().equals(PaymentStatus.PENDING)) {
                payment.setStatus(PaymentStatus.fromCode(payload.getData().getCode()));
                payment.setDate(LocalDateTime.now());
                paymentRepository.save(payment);
                List<Order> orders = payment.getOrders();
                for (Order order : orders) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                }
            }else {
                throw new RuntimeException("Handle webhook failed, payment is not pending");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Log the received payload
        log.info("Received webhook payload: {} " , payload);
        return Map.of("success", true);
    }

    @Override
    public PaymentDTO get(Long paymentId) {
        try {
            return PaymentMapper.toPaymentDTO(paymentRepository.getReferenceById(paymentId));
        }catch (Exception e){
            log.info("Error while getting payment:", e);
            throw new RuntimeException("Error while getting payment");
        }

    }

    @Override
    public PaymentDTO getPaymentLinkInformation(Long paymentId) {
        try {
            PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(paymentId);
            Payment payment = paymentRepository.getReferenceById(paymentId);
            if (paymentLinkData != null && !paymentLinkData.getStatus().equals(payment.getStatus().toString())) {
                log.info("Payment link information: {}", paymentLinkData.getStatus());
                payment.setStatus(PaymentStatus.valueOf(paymentLinkData.getStatus()));
                return PaymentMapper.toPaymentDTO(paymentRepository.save(payment));
            }else {
                return PaymentMapper.toPaymentDTO(payment);
            }
        } catch (Exception e) {
            log.info("Error while getting payment link information:", e);
            throw new RuntimeException(e);
        }
    }
}
