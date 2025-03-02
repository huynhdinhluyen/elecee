package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.service.user.AuthenticationService;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication API", description = "APIs for Authentication actions")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @GetMapping("/social-login")
    @Operation(summary = "Get social login URL")
    public ResponseEntity<String> googleLogin(@RequestParam("login_type") String loginType) {
        if (loginType.equals("google")) {
            return ResponseEntity.ok(authenticationService.generateAuthUrl(loginType));
        } else {
            throw new RuntimeException("Invalid login type");
        }
    }

    @GetMapping("/social/callback")
    @Operation(summary = "Callback for social login",
            description = "Callback for social login by the code from social login, return JWT token")
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
    @Operation(summary = "Login with username and password",
            description = "Login with username and password, now only available for staff, return JWT token")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(authenticationService.login(userLoginRequest));
    }
}