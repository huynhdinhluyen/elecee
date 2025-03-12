package com.example.electrical_preorder_system_backend.mapper;

import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import com.example.electrical_preorder_system_backend.dto.response.UserListDTO;
import com.example.electrical_preorder_system_backend.entity.User;

import java.util.List;

public class UserMapper {

    public static UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .role(user.getRole())
                .address(user.getAddress())
                .isVerified(user.isVerified())
                .isDeleted(user.isDeleted())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static UserListDTO toUserListDTO(List<User> users, int totalPages, long totalElements, int currentPage, int pageSize) {
        return UserListDTO.builder()
                .users(
                        users.stream()
                                .map(UserMapper::toUserDTO)
                                .toList()
                )
                .totalPages(totalPages)
                .totalElements(totalElements)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .build();
    }

}
