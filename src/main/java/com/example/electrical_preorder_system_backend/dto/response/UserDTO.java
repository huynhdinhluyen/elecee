package com.example.electrical_preorder_system_backend.dto.response;

import com.example.electrical_preorder_system_backend.entity.Order;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor
public class UserDTO {

     UUID id;
     String name;
     String email;
     String phoneNumber;
     UserStatus status;
     UserRole role;
     boolean isVerified = false;
//    private String token;
//    private LocalDateTime tokenExpires;
//    private List<Order> orders = new ArrayList<>();
     LocalDateTime createdAt;
     LocalDateTime updatedAt;
}
