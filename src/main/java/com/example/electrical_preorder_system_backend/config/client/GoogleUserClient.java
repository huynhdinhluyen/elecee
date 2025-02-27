package com.example.electrical_preorder_system_backend.config.client;

import com.example.electrical_preorder_system_backend.dto.response.GoogleUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "google-user", url = "https://www.googleapis.com")
public interface GoogleUserClient {

    @GetMapping(value = "/oauth2/v1/userinfo")
    GoogleUserResponse getUserInfo(@RequestParam("alt") String alt,
                                   @RequestParam("access_token") String accessToken);
}
