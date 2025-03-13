package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.order.CreateOrderRequest;
import com.example.electrical_preorder_system_backend.dto.request.order.UpdateOrderRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.service.order.OrderService;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
@Tag(name = "Order API", description = "APIs for Order actions")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final UserService userService;
    private final OrderService orderService;

    @PostMapping()
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
    ) {
        log.info("Received request: {}", createOrderRequest);
        log.info("Create order request: {} - {}", createOrderRequest.getQuantity(), createOrderRequest.getCampaignId());
        return ResponseEntity.ok(new ApiResponse("Order created successfully",
                orderService.createOrder(userService.getAuthenticatedUser(), createOrderRequest)));
    }

    @GetMapping()
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all orders, only accessible by admin")
    public ResponseEntity<ApiResponse> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "PENDING")String status,
            @RequestParam(defaultValue = "false") boolean isDeleted,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) LocalDateTime createdAtMin,
            @RequestParam(required = false) LocalDateTime createdAtMax,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID campaignId,
            @RequestParam(required = false) LocalDateTime expectedDeliveryDateMin,
            @RequestParam(required = false) LocalDateTime expectedDeliveryDateMax
    ) {
        return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully",
                orderService.getOrders(
                        page, size, status, isDeleted, sortField, sortDirection, createdAtMin, createdAtMax, userId, campaignId,
                        expectedDeliveryDateMin, expectedDeliveryDateMax
                )));
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update an order, only accessible by customer and admin")
    public ResponseEntity<ApiResponse> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderRequest updateOrderRequest
    ) {
        orderService.update(userService.getAuthenticatedUser(), id, updateOrderRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete an order, only accessible by customer and admin")
    public ResponseEntity<ApiResponse> deleteOrder(
            @PathVariable UUID id
    ) {
        orderService.delete(userService.getAuthenticatedUser(), id);
        return ResponseEntity.noContent().build();
    }

}
