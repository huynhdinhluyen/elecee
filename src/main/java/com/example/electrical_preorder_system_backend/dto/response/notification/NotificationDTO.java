package com.example.electrical_preorder_system_backend.dto.response.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class NotificationDTO implements Serializable {
    UUID id;
    String type;
    String title;
    String body;
    String imageUrl;
    UUID userId;
    boolean isRead;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
