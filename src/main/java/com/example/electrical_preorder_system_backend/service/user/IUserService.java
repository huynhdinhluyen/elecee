package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UserRegisterRequest;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;

public interface IUserService {

    UserDTO registerUser(UserRegisterRequest userRegisterRequest);

    UserDTO verifyAccount(String token);
}
