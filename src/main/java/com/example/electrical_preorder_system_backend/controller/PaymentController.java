package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.payment.CreatePaymentRequest;
import com.example.electrical_preorder_system_backend.dto.request.payment.PaymentPayload;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.enums.PaymentMethod;
import com.example.electrical_preorder_system_backend.enums.PaymentStatus;
import com.example.electrical_preorder_system_backend.service.payment.PaymentService;
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

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/payments")
@Tag(name = "Payment API", description = "APIs for Payment actions")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping()
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new payment")
    public ResponseEntity<ApiResponse> create(
            @Valid @RequestBody CreatePaymentRequest createPaymentRequest
            ) {
        return ResponseEntity.ok(new ApiResponse("Payment created successfully",
               paymentService.createPaymentLink(userService.getAuthenticatedUser(), createPaymentRequest)));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get payment by id")
    public ResponseEntity<ApiResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse("Payment retrieved successfully", paymentService.get(id)));
    }

    @GetMapping("/payment-link/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get payment link information by id")
    public ResponseEntity<ApiResponse> getPaymentLinkInformation(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse("Payment retrieved successfully", paymentService.getPaymentLinkInformation(id)));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Handle webhook call from PayOS")
    public ResponseEntity<Map<String,Boolean>> handleWebhook(@RequestBody PaymentPayload payload) {
        log.info("Handling webhook: {}", payload);
        return ResponseEntity.ok(paymentService.handleWebhook(payload));
    }

    @GetMapping()
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all payments")
    public ResponseEntity<ApiResponse> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) LocalDateTime createdAtFrom,
            @RequestParam(required = false) LocalDateTime createdAtTo,
            @RequestParam(required = false) UUID userId
    ) throws AccessDeniedException {
        return ResponseEntity.ok(new ApiResponse("Payments retrieved successfully",
                paymentService.getPayments(
                        page, size, sortDirection, sortField, productId, amountFrom,
                        amountTo, status, method, createdAtFrom, createdAtTo, userId)));
    }
}
