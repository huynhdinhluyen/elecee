package com.example.electrical_preorder_system_backend.config.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_QUEUE = "notification_queue";
    public static final String MARK_AS_READ_QUEUE = "mark_as_read_queue";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean Queue markAsReadQueue() {
        return new Queue(MARK_AS_READ_QUEUE, true);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of(
                "com.example.electrical_preorder_system_backend.dto.request.NotificationSendRequest",
                "com.example.electrical_preorder_system_backend.dto.response.ResponseNotificationDTO",
                "com.example.electrical_preorder_system_backend.dto.request.UpdateNotificationRequest",
                "java.util.UUID",
                "java.util.Map",
                "java.util.HashMap",
                "java.util.LinkedHashMap"));
        factory.setMessageConverter(converter);

        return factory;
    }
}
