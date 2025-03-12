package com.example.electrical_preorder_system_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserListDTO {
    private List<UserDTO> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
