package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;

public interface IAuthenticationService {

    String generateAuthUrl(String loginType);

    AuthenticationResponse login(UserLoginRequest userLoginRequest);

}
