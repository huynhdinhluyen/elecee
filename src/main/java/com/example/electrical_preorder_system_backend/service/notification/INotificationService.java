package com.example.electrical_preorder_system_backend.service.notification;

import com.example.electrical_preorder_system_backend.dto.request.NotificationSendRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateNotificationRequest;
import com.example.electrical_preorder_system_backend.dto.response.NotificationListDTO;
import com.example.electrical_preorder_system_backend.entity.Notification;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface INotificationService {

    /**
     * Push notification to a user
     * No return value, just log error if something goes wrong
     * Use RabbitMQ to send notification
     *
     * @param request Notification request containing user id, type, title, body, and data
     *
     */
    void pushNotification(NotificationSendRequest request);

    /**
     * Save notification to database
     * No return value, just log error if something goes wrong
     *
     * @param id    Notification id
     * @param type  Notification type
     * @param title Notification title
     * @param body  Notification body
     * @param data  Additional data
     *
     */
    void saveNotification(UUID id, String type, String title, String body, Map<String, String> data);

    /**
     * Push notifications to multiple users
     * No return value, just log error if something goes wrong
     *
     * @param ids     List of user ids
     * @param type    Notification type
     * @param imageUrl Image URL
     * @param title   Notification title
     * @param body    Notification body
     * @param data    Additional data
     *
     */
    void pushNotifications(List<UUID> ids, String type, String imageUrl, String title, String body, Map<String, String> data);

    /**
     * Get notifications of a user
     *
     * @param id   User id
     * @param page Page number
     * @param size Number of items per page
     * @return Page of notifications
     *
     */
    NotificationListDTO getNotifications(UUID id, int page, int size);

    /**
     * Mark notification as read
     * No return value, just log error if something goes wrong
     *
     * @param id Notification id
     *
     */
    void markAsRead(UUID id);

    /**
     * Mark all notifications as read
     * No return value, just log error if something goes wrong
     *
     * @param id List of notification ids
     *
     */
    void markAsAllRead(List<UUID> id);

    /**
     * Update notification
     * No return value, just log error if something goes wrong
     *
     * @param id      Notification id
     * @param request Update notification request, now only support read status
     *
     */
    void updateNotification(UUID id , UpdateNotificationRequest request);
}
