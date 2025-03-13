package com.example.electrical_preorder_system_backend.service.notification;

import com.example.electrical_preorder_system_backend.config.rabbitmq.RabbitMQConfig;
import com.example.electrical_preorder_system_backend.dto.request.NotificationSendRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateNotificationRequest;
import com.example.electrical_preorder_system_backend.dto.response.NotificationListDTO;
import com.example.electrical_preorder_system_backend.entity.DeviceToken;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.entity.Notification;
import com.example.electrical_preorder_system_backend.enums.NotificationType;
import com.example.electrical_preorder_system_backend.mapper.NotificationMapper;
import com.example.electrical_preorder_system_backend.repository.NotificationRepository;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import com.example.electrical_preorder_system_backend.service.RabbitMQService;
import com.example.electrical_preorder_system_backend.util.Validator;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService implements INotificationService{

    private final UserRepository userRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepository notificationRepository;
    private final RabbitMQService rabbitMQService;

    @Override
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void pushNotification(NotificationSendRequest request) {
        if (Validator.isValidNotificationType(request.getType())) {
            log.error("Invalid notification type");
            return;
        }
        User user = null;
        try{
            user =  userRepository.findById(request.getUserId()).get();
        }catch (Exception e){
            log.error("User [{}] not found", request.getUserId());
        }
        if (user == null) {
            log.error("User [{}] not found", request.getUserId());
            return;
        }
        //Save notification to database
        try {
            saveNotification(request.getUserId(), request.getType(), request.getTitle(), request.getBody(), request.getData());
        } catch (Exception e) {
            log.error("Failed to add notification: {}", e.getMessage());
        }
        List<DeviceToken> deviceTokenList = user.getDeviceTokens();
        List<String> tokens = new ArrayList<>();
        for (DeviceToken deviceToken : deviceTokenList) {
            if (deviceToken.getToken() != null && !deviceToken.isDeleted()) {
                tokens.add(deviceToken.getToken());
            }
        }
        if (tokens.isEmpty()) {
            log.error("No device token found for user [{}]", request.getUserId());
            return;
        }
        //Send notification to FCM
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .setImage(request.getImageUrl())
                        .build())
                .addAllTokens(tokens)
                .putAllData(request.getData())
                .build();
        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
            log.info("Successfully sent notification: Success={}, Failure={}",
                    response.getSuccessCount(), response.getFailureCount());
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        FirebaseMessagingException exception = responses.get(i).getException();
                        log.error("Failed to send to token {}: ErrorCode={}, Message={}",
                                tokens.get(i), exception != null ? exception.getErrorCode() : "Unknown",
                                exception != null ? exception.getMessage() : "No details");
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Error sending notification: {}", e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "notifications", key = "#id")
    public void saveNotification(UUID id, String type, String title, String body, Map<String, String> data) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User not found"));
        com.example.electrical_preorder_system_backend.entity.Notification notification =
                com.example.electrical_preorder_system_backend.entity.Notification.builder()
                        .title(title)
                        .type(NotificationType.valueOf(type))
                        .body(body)
                        .user(user)
                        .data((HashMap<String, String>) data)
                        .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void pushNotifications(List<UUID> ids, String type, String imageUrl, String title, String body, Map<String, String> data) {
        if(Validator.isValidNotificationType(type)) {
            throw new RuntimeException("Invalid notification type");
        }
        if (ids.isEmpty()) {
            throw new RuntimeException("No user id provided");
        }
        for (UUID id : ids) {
            rabbitMQService.queueNotification(id, type, imageUrl, title, body, data);
        }
    }

    @Override
    public NotificationListDTO getNotifications(UUID id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Notification> notifications = notificationRepository.findAllByUserId(id, pageable);
        List<Notification> notificationList = notifications.getContent();
        return NotificationMapper.toNotificationListDTO(
                notificationList,
                notifications.getTotalPages(),
                notifications.getTotalElements(),
                notifications.getNumber(),
                notifications.getSize());
    }

    @Override
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.MARK_AS_READ_QUEUE)
    public void markAsRead(UUID id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        if (notification.isPresent()) {
            notification.get().setRead(true);
            notificationRepository.save(notification.get());
        }else {
            log.error("Notification [{}] not found to mark as read", id);
        }
    }

    @Override
    @Transactional
    public void markAsAllRead(List<UUID> id) {
        for (UUID uuid : id) {
            markAsRead(uuid);
        }
    }

    @Override
    @Transactional
    public void updateNotification(UUID id , UpdateNotificationRequest request){
        rabbitMQService.queueMarkAsRead(id);
    }

}
