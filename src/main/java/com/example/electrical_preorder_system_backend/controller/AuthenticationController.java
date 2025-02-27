package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.service.user.AuthenticationService;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @GetMapping("/social-login")
    public ResponseEntity<String> googleLogin(@RequestParam("login_type") String loginType) {
        if (loginType.equals("google")) {
            return ResponseEntity.ok(authenticationService.generateAuthUrl(loginType));
        } else {
            throw new RuntimeException("Invalid login type");
        }
    }

    @GetMapping("/social/callback")
    public ResponseEntity<AuthenticationResponse> callback(
            @RequestParam("code") String code,
            @RequestParam("login_type") String loginType
    ) {
        loginType = loginType.trim().toLowerCase();
        if (loginType.equals("google")) {
            return ResponseEntity.ok(userService.googleLogin(code));
        } else {
            throw new RuntimeException("Invalid login type");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(authenticationService.login(userLoginRequest));
    }
}