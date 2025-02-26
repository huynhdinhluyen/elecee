package com.example.electrical_preorder_system_backend.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {
    String fullname;
    String phoneNumber;
    String address;
    String currentPassword;
    String newPassword;
}
