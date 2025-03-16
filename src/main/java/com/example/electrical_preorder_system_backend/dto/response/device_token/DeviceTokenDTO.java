package com.example.electrical_preorder_system_backend.dto.response.device_token;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceTokenDTO {
    UUID id;
    UUID userId;
    String token;
    boolean isDeleted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
