package com.example.electrical_preorder_system_backend.config;

import com.example.electrical_preorder_system_backend.entity.ApiPermission;
import com.example.electrical_preorder_system_backend.entity.ApiRole;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.repository.ApiRoleRepository;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import com.example.electrical_preorder_system_backend.service.user.ApiPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Autowired
    private ApiRoleRepository apiRoleRepository;

    @Autowired
    private ApiPermissionService apiPermissionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner init() {
        return args -> {
            // Initialize roles if not present
            List<String> roles = List.of("ROLE_ADMIN", "ROLE_CUSTOMER", "ROLE_STAFF", "PERMIT_ALL");
            for (String roleName : roles) {
                apiRoleRepository.findByRoleName(roleName)
                        .orElseGet(() -> apiRoleRepository.save(new ApiRole(roleName)));
            }
            log.info("Role init");

            // Retrieve roles after insertion
            ApiRole roleAdmin = apiRoleRepository.findByRoleName("ROLE_ADMIN").orElseThrow();
            ApiRole roleCustomer = apiRoleRepository.findByRoleName("ROLE_CUSTOMER").orElseThrow();
            ApiRole roleStaff = apiRoleRepository.findByRoleName("ROLE_STAFF").orElseThrow();
            ApiRole rolePermitAll = apiRoleRepository.findByRoleName("PERMIT_ALL").orElseThrow();

            // Default permissions
            List<ApiPermission> permissions = List.of(
                    new ApiPermission("POST", apiPrefix + "/user", Set.of(roleAdmin)),
                    new ApiPermission("POST", apiPrefix + "/user/sign-up", Set.of(roleAdmin)),
                    new ApiPermission("PUT", apiPrefix + "/products", Set.of(roleAdmin, roleStaff)),
                    new ApiPermission("DELETE", apiPrefix + "/products", Set.of(roleAdmin)),
                    new ApiPermission("POST", apiPrefix + "/products", Set.of(roleAdmin, roleStaff)),
                    new ApiPermission("GET", apiPrefix + "/auth/social-login", Set.of(rolePermitAll)),
                    new ApiPermission("GET", apiPrefix + "/auth/social/callback", Set.of(rolePermitAll)),
                    new ApiPermission("POST", apiPrefix + "/auth/login", Set.of(rolePermitAll)),
                    new ApiPermission("GET", apiPrefix + "/products", Set.of(rolePermitAll)),
                    new ApiPermission("GET", apiPrefix + "/categories", Set.of(rolePermitAll)),
                    new ApiPermission("POST", apiPrefix + "/categories", Set.of(roleAdmin)),
                    new ApiPermission("PUT", apiPrefix + "/categories", Set.of(roleAdmin)),
                    new ApiPermission("DELETE", apiPrefix + "/categories", Set.of(roleAdmin))
            );

            // Add permissions if not present in the database
            for (ApiPermission permission : permissions) {
                apiPermissionService.createPermission(
                        permission.getHttpMethod(),
                        permission.getPathPattern(),
                        permission.getRoles()
                );
            }
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
