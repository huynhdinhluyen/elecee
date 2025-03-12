package com.example.electrical_preorder_system_backend.entity;

import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "\"user\"", indexes = {
        @Index(name = "idx_user_name", columnList = "username"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_phone_number", columnList = "phone_number")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @Size(min = 3, max = 50)
    private String username;

    @Column(nullable = false)
    @Size(min = 3, max = 100)
    private String fullname;

    @Column(unique = true)
    @Email
    private String email;

    @Column(unique = true)
    private String googleAccountId;

    private String phoneNumber;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean isVerified;

    @Column(nullable = true)
    private boolean isDeleted = false;

    @Column(length = 512)
    private String token;

    private LocalDateTime tokenExpires;

    private String address;

    private String avatar;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DeviceToken> deviceTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
