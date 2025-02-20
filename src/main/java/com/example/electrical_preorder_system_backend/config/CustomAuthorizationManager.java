package com.example.electrical_preorder_system_backend.config;

import com.example.electrical_preorder_system_backend.entity.ApiPermission;
import com.example.electrical_preorder_system_backend.entity.ApiRole;
import com.example.electrical_preorder_system_backend.service.user.ApiPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Component
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthorizationManager.class);
    private final ApiPermissionService permissionService;

    public CustomAuthorizationManager(ApiPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        String requestPath = context.getRequest().getRequestURI();
        String requestMethod = context.getRequest().getMethod();

        List<ApiPermission> permissions = permissionService.getAllPermissionsWithRoles();
//        for (ApiPermission permission : permissions) {
//            log.info("Permission: {} - {} - {}", permission.getHttpMethod(), permission.getPathPattern(), permission.getRoles());
//        }
        for (ApiPermission permission : permissions) {
            if (requestPath.matches(permission.getPathPattern()) &&
                    requestMethod.equalsIgnoreCase(permission.getHttpMethod())) {

                Set<ApiRole> roles = permission.getRoles();
                Authentication auth = authentication.get();

                if (roles.stream().anyMatch(role -> role.getRoleName().equals("PERMIT_ALL"))) {
                    return new AuthorizationDecision(true);
                }

                boolean hasAuthority = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> roles.stream()
                                .anyMatch(role -> role.getRoleName().equals(grantedAuthority.getAuthority())));

                if (hasAuthority) {
                    return new AuthorizationDecision(true);
                } else {
                    throw new AccessDeniedException("You do not have the permission for this action.");
                }
            }
        }
        return new AuthorizationDecision(false);
    }
}