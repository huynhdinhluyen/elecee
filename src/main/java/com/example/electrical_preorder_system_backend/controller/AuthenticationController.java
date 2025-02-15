package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.EmailVerificationRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.service.user.AuthenticationService;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @GetMapping("/social-login")
    public ResponseEntity<String> googleLogin(@RequestParam("login_type") String loginType) {
        loginType = loginType.trim().toLowerCase();
        return ResponseEntity.ok(authenticationService.generateAuthUrl(loginType));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest
    ) throws MessagingException {
        return ResponseEntity.ok(new AuthenticationResponse(userService.login(userLoginRequest)));
    }

    @GetMapping("/social/callback")
    public ResponseEntity<AuthenticationResponse> callback(
            @RequestParam("code") String code,
            @RequestParam("login_type") String loginType
    ) throws Exception {

        loginType = loginType.trim().toLowerCase();
        Map<String, Object> user = authenticationService.authenticateAndFetchUser(code, loginType);

        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        String googleAccountId = "";
        String name = "";
        String email = "";

        if (loginType.equals("google")) {
            googleAccountId = (String) Objects.requireNonNullElse(user.get("sub"), "");
            name = (String) Objects.requireNonNullElse(user.get("name"), "");
            email = (String) Objects.requireNonNullElse(user.get("email"), "");
        }

        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username(name)
                .password("")
                .email(email)
                .build();

        if (loginType.equals("google")) {
            userLoginRequest.setGoogleAccountId(googleAccountId);
        }

        log.info("User login request: {}", userLoginRequest.toString());
        return this.login(userLoginRequest);

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(
        @NonNull @RequestBody EmailVerificationRequest request
    ){
        try{
            log.info("Email verification request: {}", request.toString());
            userService.verifyEmail(request.getToken());
            return ResponseEntity.ok("Email verified successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
