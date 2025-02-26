package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.service.user.AuthenticationService;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/social-login")
    public ResponseEntity<String> googleLogin(@RequestBody String loginType) {
        loginType = loginType.trim().toLowerCase();
        return ResponseEntity.ok(authenticationService.generateAuthUrl(loginType));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest
    ) {
        try {
            String token = userService.googeLogin(userLoginRequest);
            return ResponseEntity.ok(new AuthenticationResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(e.getMessage());
        }
    }

    @GetMapping("/social/callback")
    public ResponseEntity<?> callback(
            @RequestParam("code") String code,
            @RequestParam("login_type") String loginType
    ) throws Exception {

        loginType = loginType.trim().toLowerCase();
        Map<String, Object> user = authenticationService.authenticateAndFetchUser(code, loginType);

        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        String googleAccountId = "";
        String fullName = "";
        String username = "";

        if (loginType.equals("google")) {
            googleAccountId = (String) Objects.requireNonNullElse(user.get("sub"), "");
            fullName = (String) Objects.requireNonNullElse(user.get("name"), "");
            username = (String) Objects.requireNonNullElse(user.get("email"), "");
        }

        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username(username)
                .password("")
                .fullName(fullName)
                .build();

        if (!googleAccountId.isEmpty()) {
            userLoginRequest.setGoogleAccountId(googleAccountId);
        } else {
            log.error("Google account id is empty");
        }
        return this.login(userLoginRequest);
    }

}
