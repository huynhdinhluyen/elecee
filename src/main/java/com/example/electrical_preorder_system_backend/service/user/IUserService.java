package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import jakarta.mail.MessagingException;

public interface IUserService {

    UserDTO signUp(UserSignUpRequest userSignInRequest) throws MessagingException;

    AuthenticationResponse googleLogin(String code) throws MessagingException;

    void verifyEmail(String token);


}
