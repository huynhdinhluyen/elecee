package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.EmailVerificationRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(
            @NonNull @RequestBody UserSignUpRequest userSignUpRequest
    ){
        try{
            User user = userService.signUp(userSignUpRequest);
            if (user == null){
                return ResponseEntity.badRequest().body("User signed up failed");
            }else {
                return ResponseEntity.ok("User signed up successfully");
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
