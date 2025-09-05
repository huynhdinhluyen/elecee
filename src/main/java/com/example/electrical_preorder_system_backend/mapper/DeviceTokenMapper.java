package com.example.electrical_preorder_system_backend.mapper;

import com.example.electrical_preorder_system_backend.dto.response.device_token.DeviceTokenDTO;
import com.example.electrical_preorder_system_backend.entity.DeviceToken;

public class DeviceTokenMapper {
    public static DeviceTokenDTO toDeviceTokenDTO(DeviceToken deviceToken) {
        return DeviceTokenDTO.builder()
                .id(deviceToken.getId())
                .userId(deviceToken.getUser().getId())
                .token(deviceToken.getToken())
                .isDeleted(deviceToken.isDeleted())
                .createdAt(deviceToken.getCreatedAt())
                .updatedAt(deviceToken.getUpdatedAt())
                .build();

    }
}
