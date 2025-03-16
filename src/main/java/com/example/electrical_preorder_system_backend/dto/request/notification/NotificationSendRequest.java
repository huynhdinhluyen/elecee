package com.example.electrical_preorder_system_backend.dto.request.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationSendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    UUID userId;
    String type;
    String imageUrl;
    String title;
    String body;
    Map<String, String> data;
}
