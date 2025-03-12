package com.example.electrical_preorder_system_backend.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("User is active"),
    INACTIVE("User is inactive"),
    BANNED("User is banned");

    private final String errorMessage;

    UserStatus(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
