package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.config.client.GoogleIdentityClient;
import com.example.electrical_preorder_system_backend.config.client.GoogleUserClient;
import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.dto.request.ExchangeTokenRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.mapper.UserMapper;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    private final GoogleIdentityClient googleIdentityClient;
    private final GoogleUserClient googleUserClient;

    @Override
    public UserDTO signUp(UserSignUpRequest userSignInRequest){
        if (userRepository.existsByUsername(userSignInRequest.getUsername())) {
            throw new RuntimeException("SignUp failed: Username already exists");
        }else if (userRepository.existsByEmail(userSignInRequest.getEmail())) {
            throw new RuntimeException("SignUp failed: Email already exists");
        }else if (userRepository.existsByPhoneNumber(userSignInRequest.getPhoneNumber())){
            throw new RuntimeException("SignUp failed: Phone number already exists");
        }
        String role = userSignInRequest.getRole();
        if (!isValidRole(role) || !role.equals(UserRole.ROLE_STAFF.name())) {
            throw new RuntimeException("SignUp failed: Invalid role");
        }
        try{
            User user =  User.builder()
                    .username(userSignInRequest.getUsername())
                    .password(passwordEncoder.encode(userSignInRequest.getPassword()))
                    .fullname(userSignInRequest.getFullname())
                    .email(userSignInRequest.getEmail())
                    .phoneNumber(userSignInRequest.getPhoneNumber())
                    .role(UserRole.valueOf(role))
                    .isVerified(false)
                    .status(userSignInRequest.isActive()? UserStatus.ACTIVE: UserStatus.INACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            String jwtToken = jwtUtils.generateTokenFromUsername(user.getUsername());
            user.setToken(jwtToken);
            user.setTokenExpires(LocalDateTime.ofInstant(jwtUtils.getExpDateFromToken(jwtToken).toInstant(), ZoneId.systemDefault()));
            return UserMapper.toUserDTO(userRepository.save(user));
        }catch(TransactionSystemException ex) {
            log.error("Transaction failed: ", ex);
            throw new RuntimeException("Sign-up failed due to transaction error", ex);
        }
    }

    @Override
    public AuthenticationResponse googleLogin(String code){
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
        }catch (Exception e){
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
        String username = jwtUtils.getSubjectFromToken(token);
        // Check if the token is not expired
        if (!expDate.before(new Date())){
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setVerified(true);
            userRepository.save(user);
        }else {
            throw new RuntimeException("Token expired");
        }
    }

}
