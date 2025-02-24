package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.entity.User;
import jakarta.mail.MessagingException;

public interface IUserService {

    User signUp(UserSignUpRequest userSignInRequest) throws MessagingException;

    String googeLogin(UserLoginRequest userLoginRequest) throws MessagingException;

    Boolean isValidGoogleId(UserLoginRequest userLoginRequest);

    void verifyEmail(String token);

}
