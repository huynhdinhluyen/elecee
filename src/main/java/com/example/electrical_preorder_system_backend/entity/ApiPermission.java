package com.example.electrical_preorder_system_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "api_permissions")
public class ApiPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    @Column(name = "path_pattern", nullable = false)
    private String pathPattern;

    @ManyToMany
    @JoinTable(
            name = "api_permission_roles",
            joinColumns = @JoinColumn(name = "api_permission_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<ApiRole> roles;

    public ApiPermission(String httpMethod, String pathPattern, Set<ApiRole> roles) {
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
        this.roles = roles;
    }

    public ApiPermission() {

    }
}
