package com.example.electrical_preorder_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableAsync
public class ElectricalPreorderSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricalPreorderSystemBackendApplication.class, args);
    }

}
