package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import jakarta.mail.MessagingException;

public interface IUserService {

    UserDTO registerUser(UserLoginRequest userLoginRequest);

    String login(UserLoginRequest userLoginRequest) throws MessagingException;

    Boolean isValidGoogleId(UserLoginRequest userLoginRequest);

    void verifyEmail(String token);
}
