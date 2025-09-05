package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.notification.NotificationSendRequest;
import com.example.electrical_preorder_system_backend.dto.request.notification.UpdateNotificationRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.service.RabbitMQService;
import com.example.electrical_preorder_system_backend.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/notifications")
@Tag(name = "Notification API", description = "APIs for Notification actions")
public class NotificationController {

    private final RabbitMQService rabbitMQService;
    private final NotificationService notificationService;

    @PostMapping("/send")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Send notification to a user, only accessible by admin",
            description = "Send notification to a user using RabbitMQ, " +
                    "type = 'ADMIN_NOTIFICATION'")
    public ResponseEntity<ApiResponse>sendNotification(
            @RequestBody NotificationSendRequest request
            ) {
        rabbitMQService.queueNotification(
                request.getUserId(),
                request.getType(),
                request.getImageUrl(),
                request.getTitle(),
                request.getBody(),
                request.getData()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Notification sent successfully",""));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get notifications of a user",
            description = "Get notifications of a user by user id")
    public ResponseEntity<ApiResponse> getNotifications(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse("Notification retrieved successfully",
                        notificationService.getNotifications(id, page, size)));
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update notification request",
            description = "Update notification request, now only support read status")
    public ResponseEntity<ApiResponse> updateNotification(
           @PathVariable UUID id ,
           @RequestBody UpdateNotificationRequest request) {
        notificationService.updateNotification(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse("Notification updated request sent successfully", ""));
    }

}
