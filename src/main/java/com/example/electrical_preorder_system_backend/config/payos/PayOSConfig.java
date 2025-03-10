package com.example.electrical_preorder_system_backend.config.payos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {

    @Value("${payos.clientId}")
    private String clientId;

    @Value("${payos.apiKey}")
    private String apiKey;

    @Value("${payos.checksumKey}")
    private String checksumKey;

    @Bean
    public PayOS payOS() {
        if (clientId == null || apiKey == null || checksumKey == null) {
            throw new IllegalStateException("One or more required environment variables are not set.");
        }
        return new PayOS(clientId, apiKey, checksumKey);
    }

}
