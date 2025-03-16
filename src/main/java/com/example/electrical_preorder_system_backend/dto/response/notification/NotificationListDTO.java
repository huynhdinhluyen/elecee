package com.example.electrical_preorder_system_backend.dto.response.notification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class NotificationListDTO {
    List<NotificationDTO> notifications;
    int totalPages;
    long totalElements;
    int currentPage;
    int pageSize;
}
