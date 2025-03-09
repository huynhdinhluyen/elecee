package com.example.electrical_preorder_system_backend.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("PENDING"),
    INVALID_PARAMS("01"),
    PAID("00"),
    CANCELLED("CANCELLED"),
    PROCESSING("PROCESSING "),
    FAILED("FAILED"),
    WEBHOOK_URL_INVALID("400"),
    MISSING_API_KEY_CLIENT_KEY("401"),
    TOO_MANY_REQUESTS("429"),
    SYSTEM_ERROR("5XX");

    private final String code;

    PaymentStatus(String code) {
        this.code = code;
    }

    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
