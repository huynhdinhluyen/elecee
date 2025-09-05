package com.example.electrical_preorder_system_backend.service.order;

import com.example.electrical_preorder_system_backend.dto.request.order.CreateOrderRequest;
import com.example.electrical_preorder_system_backend.dto.request.order.UpdateOrderRequest;
import com.example.electrical_preorder_system_backend.dto.response.order.OrderDTO;
import com.example.electrical_preorder_system_backend.dto.response.order.OrderListDTO;
import com.example.electrical_preorder_system_backend.entity.*;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import com.example.electrical_preorder_system_backend.enums.OrderStatus;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.mapper.OrderMapper;
import com.example.electrical_preorder_system_backend.repository.CampaignRepository;
import com.example.electrical_preorder_system_backend.repository.CampaignStageRepository;
import com.example.electrical_preorder_system_backend.repository.OrderRepository;
import com.example.electrical_preorder_system_backend.repository.ProductRepository;
import com.example.electrical_preorder_system_backend.repository.specification.OrderSpecification;
import com.example.electrical_preorder_system_backend.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignStageRepository campaignStageRepository;

    @Override
    @Transactional
    public OrderDTO createOrder(User user, CreateOrderRequest createOrderRequest) {
        Campaign campaign = campaignRepository.findActiveCampaignById(createOrderRequest.getCampaignId());
        if (!isValidCampaignToOrder(campaign)) {
            throw new IllegalArgumentException("Invalid campaign to order");
        }
        if (!isValidQuantity(campaign.getProduct(), createOrderRequest.getQuantity())) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        //Update product quantity and create new order
        try{
            Product product = campaign.getProduct();
            product.setQuantity(product.getQuantity() - createOrderRequest.getQuantity());
            productRepository.save(product);

            List<Order> orders = orderRepository.findByUserIdAndCampaignId(user.getId(), campaign.getId());
//            Order order = orders.stream().filter(o -> o.getStatus().equals(OrderStatus.PENDING)).findFirst().orElse(null);
//            if (order != null && order.getStatus().equals(OrderStatus.PENDING)) {//If order already exists, update quantity and total amount
//                order.setQuantity(order.getQuantity() + createOrderRequest.getQuantity());
//                order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
//            }else{//Create new order

            Order order = Order.builder()
                .user(user)
                .campaign(campaign)
                .quantity(createOrderRequest.getQuantity())
                //Total amount = product price * quantity
                .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(createOrderRequest.getQuantity())))
                .status(OrderStatus.PENDING)
                .build();
//            }
            return OrderMapper.toOrderDTO(orderRepository.save(order));

        }catch (Exception e){
            log.info("Failed to create order: {} ", e.getMessage());
            throw new RuntimeException("Failed to create order");
        }
    }

    @Override
    public OrderListDTO getOrders(int page, int size, String status, boolean isDeleted, String sortField, String sortDirection, LocalDateTime createdAtMin,
                                  LocalDateTime createdAtMax, UUID userId, UUID campaignId,
                                  LocalDateTime expectedDeliveryDateMin,
                                  LocalDateTime expectedDeliveryDateMax) {
        String errorResponseMessage = Validator.verifyOrderFilter(page, size, status, sortField, sortDirection, Order.class);
        if (errorResponseMessage != null) {
            throw new IllegalArgumentException(errorResponseMessage);
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Order> spec = Specification.where(
                OrderSpecification.hasCampaignId(campaignId))
                .and(OrderSpecification.hasUserId(userId))
                .and(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.hasCreatedBetween(createdAtMin, createdAtMax))
                .and(OrderSpecification.hasExpectedDeliveryDateBetween(expectedDeliveryDateMin, expectedDeliveryDateMax));
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        long totalAmount = orderPage.stream().mapToLong(order -> order.getTotalAmount().longValue()).sum();
        return OrderMapper.toOrderListDTO(
                orderPage.getContent(),
                totalAmount,
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getNumber(),
                orderPage.getSize()
        );
    }

    @Override
    @Transactional
    public void update(User user, UUID orderId, UpdateOrderRequest updateOrderRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        //Only customer who created the order or admin can update order
        if (!order.getUser().getId().equals(user.getId()) && !user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new IllegalArgumentException("Unauthorized to update order");
        }
        if (!isValidQuantity(order.getCampaign().getProduct(), updateOrderRequest.getQuantity())) {
            throw new IllegalArgumentException("Invalid quantity");
        }
        //Only pending order can be updated
        if (order.getStatus().equals(OrderStatus.PENDING)) {
            Product product = order.getCampaign().getProduct();
            product.setQuantity(product.getQuantity() + order.getQuantity() - updateOrderRequest.getQuantity());
            productRepository.save(product);
            order.setQuantity(updateOrderRequest.getQuantity());
            order.setTotalAmount(order.getCampaign().getProduct().getPrice().multiply(BigDecimal.valueOf(updateOrderRequest.getQuantity())));
            orderRepository.save(order);
        }else {
            throw new IllegalArgumentException("Order cannot be updated");
        }
    }

    @Override
    public void delete(User user, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        //Only customer who created the order or admin can delete order
        if (!order.getUser().getId().equals(user.getId()) && !user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new IllegalArgumentException("Unauthorized to delete order");
        }
        //Only pending order can be deleted
        if (order.getStatus().equals(OrderStatus.PENDING)) {
            Product product = order.getCampaign().getProduct();
            product.setQuantity(product.getQuantity() + order.getQuantity());
            productRepository.save(product);
            order.setDeleted(true);
            orderRepository.save(order);
        }else {
            throw new IllegalArgumentException("Order cannot be deleted");
        }
    }

    private boolean isValidQuantity(Product product, Integer quantity) {
        return quantity > 0 && quantity < product.getQuantity();
    }

    private boolean isValidCampaignToOrder(Campaign campaign) {
        return campaign != null && !campaign.isDeleted() && campaign.getStatus().equals(CampaignStatus.ACTIVE);
    }

}
