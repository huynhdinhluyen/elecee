package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public interface IAuthenticationService {

    String generateAuthUrl(String loginType);

    Map<String,Object> authenticateAndFetchUser(String code, String loginType) throws Exception;
}
