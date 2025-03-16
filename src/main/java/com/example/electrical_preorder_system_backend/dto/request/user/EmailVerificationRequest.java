package com.example.electrical_preorder_system_backend.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class EmailVerificationRequest {
    String token;
}
