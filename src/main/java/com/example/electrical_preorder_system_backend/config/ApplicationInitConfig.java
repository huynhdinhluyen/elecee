package com.example.electrical_preorder_system_backend.config;

import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${default.admin.username}")
    private String defaultAdminUsername;

    @Value("${default.admin.password}")
    private String defaultAdminPassword;

    @Value("${default.admin.fullname}")
    private String defaultAdminFullname;

    @Bean
    ApplicationRunner init() {
        return args -> {
            if (userRepository.findByUsername(defaultAdminUsername).isEmpty()) {
                User user = new User();
                user.setUsername(defaultAdminUsername);
                user.setFullname(defaultAdminFullname);
                user.setPassword(passwordEncoder.encode(defaultAdminPassword));
                user.setVerified(true);
                user.setStatus(UserStatus.ACTIVE);
                user.setRole(UserRole.ROLE_ADMIN);
                userRepository.save(user);
                log.info("User init");
            }
        };
    }

}