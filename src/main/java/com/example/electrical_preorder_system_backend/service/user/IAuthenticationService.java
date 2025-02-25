package com.example.electrical_preorder_system_backend.service.user;

import java.util.Map;

public interface IAuthenticationService {

    String generateAuthUrl(String loginType);

    Map<String,Object> authenticateAndFetchUser(String code, String loginType) throws Exception;
}
