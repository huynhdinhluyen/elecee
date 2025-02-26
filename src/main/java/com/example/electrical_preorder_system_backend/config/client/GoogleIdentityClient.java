package com.example.electrical_preorder_system_backend.config.client;

import com.example.electrical_preorder_system_backend.dto.request.ExchangeTokenRequest;
import com.example.electrical_preorder_system_backend.dto.response.ExchangeTokenResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "google-identity", url = "https://oauth2.googleapis.com")
public interface GoogleIdentityClient {

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest request);
}
