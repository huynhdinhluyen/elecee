package com.example.electrical_preorder_system_backend.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

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