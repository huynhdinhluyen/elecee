package com.example.electrical_preorder_system_backend.service.payment;

import com.example.electrical_preorder_system_backend.dto.request.payment.CreatePaymentRequest;
import com.example.electrical_preorder_system_backend.dto.request.payment.PaymentPayload;
import com.example.electrical_preorder_system_backend.dto.response.payment.PaymentDTO;
import com.example.electrical_preorder_system_backend.dto.response.payment.PaymentListDTO;
import com.example.electrical_preorder_system_backend.entity.*;
import com.example.electrical_preorder_system_backend.enums.*;
import com.example.electrical_preorder_system_backend.mapper.PaymentMapper;
import com.example.electrical_preorder_system_backend.repository.*;
import com.example.electrical_preorder_system_backend.repository.specification.PaymentSpecification;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import com.example.electrical_preorder_system_backend.util.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final CampaignStageRepository campaignStageRepository;

    private static final int MAX_RETRY_CREATE_PAYMENT = 10;

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
    private final ProductRepository productRepository;
    private final UserService userService;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public CheckoutResponseData createPaymentLink(User user, List<ItemData> items , Integer amount, Payment payment, int retryCreatePayment) {
        if(retryCreatePayment > MAX_RETRY_CREATE_PAYMENT){
            throw new RuntimeException("Error while creating payment");
        }
        try {
            long currentSeconds = (int) (System.currentTimeMillis()/1000);
            long randomPaymentId = (long) (Math.random() * 1000000000);
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(randomPaymentId)
                    .amount(amount)
                    .buyerEmail(user.getEmail())
                    .description(UUID.randomUUID().toString().substring(0, 24))
                    .items(items)
                    .returnUrl(DEFAULT_PAYMENT_RETURN_URL)
                    .cancelUrl(DEFAULT_PAYMENT_CANCEL_URL)
                    .expiredAt(currentSeconds + DEFAULT_PAYMENT_EXPIRE_TIME)
                    .build();
            CheckoutResponseData responseData =  payOS.createPaymentLink(paymentData);
            payment.setId(responseData.getOrderCode());
            Payment savedPayment =  paymentRepository.save(payment);
            for(Order order : savedPayment.getOrders()){
                order.getPayments().add(savedPayment);
                orderRepository.save(order);
            }
            return responseData;
        }catch (PayOSException e){
            log.info("Error while creating payment link: ", e);
            return createPaymentLink(user, items, amount, payment, retryCreatePayment + 1);
        }catch (Exception e){
            log.info("Error while creating payment link: ", e);
            throw new RuntimeException("Error while creating payment link");
        }

    }

    @Override
    @Transactional
    public CheckoutResponseData createPaymentLink(User user, CreatePaymentRequest createPaymentRequest) {
            Payment payment = new Payment();
            payment.setMethod(createPaymentRequest.getMethod());
            payment.setStatus(PaymentStatus.PENDING);
            List<ItemData> items = new ArrayList<>();
            List<Order> orders = orderRepository.findAllById(createPaymentRequest.getOrderIds());
            for (Order order : orders) {
                if (order.getStatus().equals(OrderStatus.PENDING)) {//Only pending orders can be paid
                    payment.setAmount(payment.getAmount().add(order.getTotalAmount()));
                    items.add(PaymentMapper.toItemData(order, order.getCampaign().getProduct()));
                } else {
                    throw new RuntimeException("Order " + order.getId() + " is not pending");
                }
            }
            if (payment.getOrders().isEmpty()) {
                payment.setOrders(new ArrayList<>());
            }
            payment.setOrders(orders);
           return createPaymentLink(user, items, payment.getAmount().intValue(),payment, 0);
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
                payment.setDate(LocalDateTime.now());
                List<Order> orders = payment.getOrders();
                for (Order order : orders) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                }
                if (payment.getStatus().equals(PaymentStatus.PAID))
                {
                    for (Order order : orders) {
                        Campaign campaign = order.getCampaign();
                        List<CampaignStage> stages = campaignStageRepository.findCampaignStagesByCampaign(campaign);
                        for (CampaignStage stage : stages) {
                            if (stage.getStatus().equals(CampaignStageStatus.ACTIVE)) {
                                stage.setQuantitySold(stage.getQuantitySold() + order.getQuantity());
                                campaignStageRepository.save(stage);
                                break;
                            }
                        }
                    }
                }
                return PaymentMapper.toPaymentDTO(paymentRepository.save(payment));
            }else {
                return PaymentMapper.toPaymentDTO(payment);
            }
        } catch (Exception e) {
            log.info("Error while getting payment link information:", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentListDTO getPayments(int page, int size, String sortDirection, String sortField, UUID productId, BigDecimal amountFrom, BigDecimal amountTo,
                                      PaymentStatus status, PaymentMethod method, LocalDateTime createdAtFrom, LocalDateTime createdAtTo,UUID userId) throws AccessDeniedException {
        String verifyFilter = Validator.verifyPaymentFilter(page, size, sortField, sortDirection, Payment.class);
        if (verifyFilter != null) {
            throw new RuntimeException(verifyFilter);
        }
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Payment> spec = Specification.where(null);
        if (productId != null) {
            try {
                productRepository.getReferenceById(productId);
            } catch (Exception e) {
                throw new RuntimeException("Product not found");
            }
            spec = spec.and(PaymentSpecification.hasProductId(productId));
        }

        User authenticatedUser = userService.getAuthenticatedUser();
        if (userId != null){
            if (!authenticatedUser.getId().equals(userId) && !authenticatedUser.getRole().equals(UserRole.ROLE_ADMIN)){
                throw new AccessDeniedException("Access denied");
            }
            spec = spec.and(PaymentSpecification.hasUserId(userId));
        }else {
            if (!authenticatedUser.getRole().equals(UserRole.ROLE_ADMIN)){
                throw new AccessDeniedException("Access denied");
            }
        }
        spec = amountFrom != null ? spec.and(PaymentSpecification.hasAmountGreaterThan(amountFrom)) : spec;
        spec = amountTo != null ? spec.and(PaymentSpecification.hasAmountLessThan(amountTo)) : spec;
        spec = status != null ? spec.and(PaymentSpecification.hasStatus(status)) : spec;
        spec = method != null ? spec.and(PaymentSpecification.hasMethod(method)) : spec;
        spec = createdAtFrom != null ? spec.and(PaymentSpecification.createdAtFrom(createdAtFrom)) : spec;
        spec = createdAtTo != null ? spec.and(PaymentSpecification.createdAtTo(createdAtTo)) : spec;
        Page<Payment> payments = paymentRepository.findAll(spec, pageable);
        BigDecimal totalAmount = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentMapper.toPaymentListDTO(
                payments.getContent(),
                totalAmount.longValue(),
                payments.getTotalPages(),
                payments.getTotalElements(),
                payments.getNumber(),
                payments.getSize()
        );
    }
}
