package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.EmailVerificationRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateUserRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import com.example.electrical_preorder_system_backend.service.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDTO> signUp(
            @NonNull @RequestBody UserSignUpRequest userSignUpRequest
    ) {
        return ResponseEntity.ok(userService.signUp(userSignUpRequest));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @NonNull @RequestBody EmailVerificationRequest request
    ) {
        try {
            log.info("Email verification request: {}", request.toString());
            userService.verifyEmail(request.getToken());
            return ResponseEntity.ok("Email verified successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserDTO> getById(
            @NonNull @PathVariable UUID id
    ){
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping()
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(new ApiResponse("Users retrieved successfully", userService.getUsers(pageable)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse> update(
            @NonNull @PathVariable UUID id,
            @NonNull @RequestBody UpdateUserRequest updateUserRequest
    ){
        userService.update(id, updateUserRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> delete(
            @NonNull @PathVariable UUID id
    ){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
