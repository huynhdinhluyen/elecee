package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.config.client.GoogleIdentityClient;
import com.example.electrical_preorder_system_backend.config.client.GoogleUserClient;
import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.dto.request.ExchangeTokenRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdatePasswordRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateUserRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.mapper.UserMapper;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import com.example.electrical_preorder_system_backend.service.email.EmailService;
import com.example.electrical_preorder_system_backend.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleIdentityClient googleIdentityClient;
    private final GoogleUserClient googleUserClient;
    private final EmailService emailService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Override

    public UserDTO signUp(UserSignUpRequest userSignInRequest) {
        if (userRepository.existsByUsername(userSignInRequest.getUsername())) {
            throw new RuntimeException("SignUp failed: Username already exists");
        } else if (userRepository.existsByEmail(userSignInRequest.getEmail())) {
            throw new RuntimeException("SignUp failed: Email already exists");
        } else if (userRepository.existsByPhoneNumber(userSignInRequest.getPhoneNumber())) {
            throw new RuntimeException("SignUp failed: Phone number already exists");
        }
        String role = userSignInRequest.getRole();
        if (!isValidRole(role) || !role.equals(UserRole.ROLE_STAFF.name())) {
            throw new RuntimeException("SignUp failed: Invalid role");
        }
        try {
            User user = User.builder()
                    .username(userSignInRequest.getUsername())
                    .password(passwordEncoder.encode(userSignInRequest.getPassword()))
                    .fullname(userSignInRequest.getFullname())
                    .email(userSignInRequest.getEmail())
                    .phoneNumber(userSignInRequest.getPhoneNumber())
                    .role(UserRole.valueOf(role))
                    .isVerified(false)
                    .status(userSignInRequest.isActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            // Send email verification
            emailService.sendEmail(user.getEmail(),
                    "Email verification",
                    emailService.bodyRegister(user.getEmail(), user.getFullname()));
            userRepository.save(user);
            String jwtToken = jwtUtils.generateTokenFromUsername(user.getUsername());
            user.setToken(jwtToken);
            user.setTokenExpires(LocalDateTime.ofInstant(jwtUtils.getExpDateFromToken(jwtToken).toInstant(), ZoneId.systemDefault()));
            return UserMapper.toUserDTO(userRepository.save(user));
        } catch (TransactionSystemException ex) {
            log.error("Transaction failed: ", ex);
            throw new RuntimeException("Sign-up failed due to transaction error", ex);
        }
    }

    @Override
    public AuthenticationResponse googleLogin(String code) {
        try {

            var response = googleIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                    .code(code)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .redirectUri(redirectUri)
                    .grantType("authorization_code")
                    .build());
            var userInfo = googleUserClient.getUserInfo("json", response.getAccessToken());
            log.info("User info: {}", userInfo);
            User user = userRepository.findByUsername(userInfo.getEmail())
                    .orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(userInfo.getEmail());
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setFullname(userInfo.getName());
                user.setEmail(userInfo.getEmail());
                user.setPhoneNumber("");
                user.setRole(UserRole.ROLE_CUSTOMER);
                user.setVerified(true);
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
            }
            String token = jwtUtils.generateTokenFromUsername(user.getUsername());
            user.setToken(token);
            user.setTokenExpires(LocalDateTime.ofInstant(jwtUtils.getExpDateFromToken(token).toInstant(), ZoneId.systemDefault()));
            userRepository.save(user);
            return new AuthenticationResponse(token);
        } catch (Exception e) {
//            e.printStackTrace();
            throw new RuntimeException("Google login failed", e);
        }
    }

    private boolean isValidRole(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void verifyEmail(String token) {
        Date expDate = jwtUtils.getExpDateFromToken(token);
        String email = jwtUtils.getSubjectFromToken(token);
        // Check if the token is not expired
        if (!expDate.before(new Date())) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setVerified(true);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Token expired");
        }
    }

    @Override
    public void update(UUID id, UpdateUserRequest updateUserRequest) {
        if (isValidToUpdate(id)) {
            throw new AuthorizationDeniedException("Access denied");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getStatus().equals(UserStatus.INACTIVE)) {
            throw new IllegalArgumentException("User is inactive, cannot update");
        }
        if (user.getStatus().equals(UserStatus.BANNED)) {
            throw new IllegalArgumentException("User is banned, cannot update");
        }
        updateBasicInfo(user, updateUserRequest);
        userRepository.save(user);
    }

    private void updateBasicInfo(User user, UpdateUserRequest updateUserRequest) {
        if (updateUserRequest.getFullname() != null && !updateUserRequest.getFullname().isEmpty()) {
            if (Validator.isValidFullname(updateUserRequest.getFullname())) {
                //OK
                user.setFullname(updateUserRequest.getFullname());
            } else {
                throw new IllegalArgumentException("Invalid fullname");
            }
        }
        if (updateUserRequest.getAddress() != null && !updateUserRequest.getAddress().isEmpty()) {
            if (Validator.isValidAddress(updateUserRequest.getAddress())) {
                //OK
                user.setAddress(updateUserRequest.getAddress());
            } else {
                throw new IllegalArgumentException("Invalid address");
            }
        }
        if (updateUserRequest.getPhoneNumber() != null && !updateUserRequest.getPhoneNumber().isEmpty()) {
            if (userRepository.existsByPhoneNumber(updateUserRequest.getPhoneNumber())) {
                throw new IllegalArgumentException("Phone number already exists");
            }
            if (Validator.isValidPhoneNumber(updateUserRequest.getPhoneNumber())) {
                //OK
                user.setPhoneNumber(updateUserRequest.getPhoneNumber());
            } else {
                throw new IllegalArgumentException("Invalid phone number");
            }
        }
    }

    @Override
    public void updatePassword(UUID id, UpdatePasswordRequest updatePasswordRequest) {
        if (isValidToUpdate(id)) {
            throw new AuthorizationDeniedException("Access denied");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (updatePasswordRequest.getCurrentPassword() != null && !updatePasswordRequest.getCurrentPassword().isEmpty()
                && updatePasswordRequest.getNewPassword() != null && !updatePasswordRequest.getNewPassword().isEmpty()) {
            if (!passwordEncoder.matches(updatePasswordRequest.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid current password");
            }
            if (!Validator.isValidPassword(updatePasswordRequest.getNewPassword())) {
                throw new IllegalArgumentException("Invalid new password");
            }
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Invalid password");
        }
    }

    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getStatus().equals(UserStatus.INACTIVE)) {
            throw new IllegalArgumentException("User is inactive, cannot delete");
        }
        if (user.getStatus().equals(UserStatus.BANNED)) {
            throw new IllegalArgumentException("User is banned, cannot delete");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals(user.getUsername())) {
            throw new RuntimeException("Cannot delete yourself");
        }
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Override
    public UserDTO getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getStatus().equals(UserStatus.INACTIVE)) {
            throw new IllegalArgumentException("User is inactive");
        }
        return UserMapper.toUserDTO(user);
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    private boolean isValidToUpdate(UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Customer can only update their own password
        User authenticatedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return (authenticatedUser.getRole().equals(UserRole.ROLE_CUSTOMER) || authenticatedUser.getRole().equals(UserRole.ROLE_STAFF)) && !authenticatedUser.getId().equals(id);
    }


}