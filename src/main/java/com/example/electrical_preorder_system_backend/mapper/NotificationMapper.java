package com.example.electrical_preorder_system_backend.mapper;

import com.example.electrical_preorder_system_backend.dto.response.notification.NotificationDTO;
import com.example.electrical_preorder_system_backend.dto.response.notification.NotificationListDTO;
import com.example.electrical_preorder_system_backend.entity.Notification;

import java.util.List;

public class NotificationMapper {

    public static NotificationDTO toNotificationDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .body(notification.getBody())
                .imageUrl(notification.getData().get("imageUrl"))
                .userId(notification.getUser().getId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public static NotificationListDTO toNotificationListDTO(List<Notification> notifications, int totalPages, long totalElements, int currentPage, int pageSize) {
        return NotificationListDTO.builder()
                .notifications(notifications.stream().map(NotificationMapper::toNotificationDTO).toList())
                .totalPages(totalPages)
                .totalElements(totalElements)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .build();
    }
}
