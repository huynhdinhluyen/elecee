package com.example.electrical_preorder_system_backend.service;

import com.example.electrical_preorder_system_backend.config.rabbitmq.RabbitMQConfig;
import com.example.electrical_preorder_system_backend.dto.request.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    public void queueNotification(UUID id, String type, String imageUrl, String title, String body, Map<String, String> data) {
        NotificationSendRequest notificationSendRequest = NotificationSendRequest.builder()
                .userId(id)
                .type(type)
                .imageUrl(imageUrl)
                .title(title)
                .body(body)
                .data(data)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, notificationSendRequest);
    }

    public void queueMarkAsRead(UUID id) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.MARK_AS_READ_QUEUE, id);
    }

}
