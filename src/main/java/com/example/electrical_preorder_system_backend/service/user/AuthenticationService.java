package com.example.electrical_preorder_system_backend.service.user;

//import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
//import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
//import com.example.electrical_preorder_system_backend.entity.User;
//import com.example.electrical_preorder_system_backend.enums.UserRole;
//import com.example.electrical_preorder_system_backend.enums.UserStatus;
//import com.example.electrical_preorder_system_backend.repository.UserRepository;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Override
    public String generateAuthUrl(String loginType) {
        String url="";

        if (loginType.equals("google")) {
            url = "https://accounts.google.com/o/oauth2/auth?"
                + "client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code&scope=email%20profile";
        }

        return url;
    }

    @Override
    public Map<String,Object> authenticateAndFetchUser(String code, String loginType) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Exchange authorization code for access token
        String tokenUrl = "https://oauth2.googleapis.com/token";
        Map<String, String> tokenRequest = Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );

        log.info("Token request: {}", tokenRequest);

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(tokenRequest),
                new ParameterizedTypeReference<>() {}
        );

        if (tokenResponse.getStatusCode().isError()) {
            log.info("Error response from token endpoint: {}", tokenResponse);
            throw new RuntimeException("Failed to retrieve access token");
        }

        String accessToken = (String) Objects.requireNonNullElse(tokenResponse.getBody().get("access_token"), "");

        // Retrieve user information
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return userInfoResponse.getBody();
    }
}
