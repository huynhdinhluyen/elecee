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

    @Bean
    ApplicationRunner init() {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User user = new User();
                user.setUsername("admin");
                user.setFullname("Admin");
                user.setPassword(passwordEncoder.encode("12345"));
                user.setVerified(true);
                user.setStatus(UserStatus.ACTIVE);
                user.setRole(UserRole.ROLE_ADMIN);
                userRepository.save(user);
                User staff = new User();
                staff.setUsername("staff");
                staff.setFullname("A Staff");
                staff.setPassword(passwordEncoder.encode("12345"));
                staff.setVerified(true);
                staff.setStatus(UserStatus.ACTIVE);
                staff.setRole(UserRole.ROLE_STAFF);
                userRepository.save(staff);
                log.info("User init");
            }
        };
    }

}
