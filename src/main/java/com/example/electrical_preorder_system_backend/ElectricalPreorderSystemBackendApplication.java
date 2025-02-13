package com.example.electrical_preorder_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class ElectricalPreorderSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricalPreorderSystemBackendApplication.class, args);
    }

}
