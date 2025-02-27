package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.entity.ApiPermission;
import com.example.electrical_preorder_system_backend.entity.ApiRole;
import com.example.electrical_preorder_system_backend.repository.ApiPermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ApiPermissionService {
    private final ApiPermissionRepository apiPermissionRepository;

    public ApiPermissionService(ApiPermissionRepository apiPermissionRepository) {
        this.apiPermissionRepository = apiPermissionRepository;
    }

    @Transactional(readOnly = true)
    public List<ApiPermission> getAllPermissionsWithRoles() {
        return apiPermissionRepository.findAllWithRoles();
    }

    @Transactional
    public void createPermission(String httpMethod, String pathPattern, Set<ApiRole> roles) {
        ApiPermission permission = new ApiPermission();
        permission.setHttpMethod(httpMethod);
        permission.setPathPattern(pathPattern);
        permission.setRoles(roles);
        apiPermissionRepository.save(permission);
    }
}
