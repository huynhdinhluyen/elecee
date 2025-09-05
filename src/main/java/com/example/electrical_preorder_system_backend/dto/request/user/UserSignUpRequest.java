package com.example.electrical_preorder_system_backend.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSignUpRequest {
    String username;
    String password;
    String fullname;
    String email;
    String phoneNumber;
    String address;
    String role;
    boolean active;
}