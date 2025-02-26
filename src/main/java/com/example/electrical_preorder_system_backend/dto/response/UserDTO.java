package com.example.electrical_preorder_system_backend.dto.response;

import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

     UUID id;
     String username;
     String fullname;
     String email;
     String phoneNumber;
     UserStatus status;
     UserRole role;
     boolean isVerified = false;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;
}
