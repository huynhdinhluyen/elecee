package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.config.client.GoogleIdentityClient;
import com.example.electrical_preorder_system_backend.config.client.GoogleUserClient;
import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.dto.request.ExchangeTokenRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdatePasswordRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateUserRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.*;
import com.example.electrical_preorder_system_backend.entity.DeviceToken;
import com.example.electrical_preorder_system_backend.entity.Order;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.mapper.DeviceTokenMapper;
import com.example.electrical_preorder_system_backend.mapper.OrderMapper;
import com.example.electrical_preorder_system_backend.mapper.UserMapper;
import com.example.electrical_preorder_system_backend.repository.DeviceTokenRepository;
import com.example.electrical_preorder_system_backend.repository.OrderRepository;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import com.example.electrical_preorder_system_backend.repository.specification.UserSpecification;
import com.example.electrical_preorder_system_backend.service.cloudinary.CloudinaryService;
import com.example.electrical_preorder_system_backend.service.email.EmailService;
import com.example.electrical_preorder_system_backend.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private final DeviceTokenRepository deviceTokenRepository;
    private final OrderRepository orderRepository;
    private final CloudinaryService cloudinaryService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Override

    public UserDTO signUp(UserSignUpRequest userSignUpRequest) {
        String invalidRequestMessage = Validator.verifyUserSignUp(userSignUpRequest);
        if (invalidRequestMessage != null) {
            throw new IllegalArgumentException(invalidRequestMessage);
        }
        if (Validator.isValidUserRole(userSignUpRequest.getRole().trim()) || !userSignUpRequest.getRole().trim().equals(UserRole.ROLE_STAFF.name())) {
            throw new RuntimeException("SignUp failed: Invalid role");
        }
        if (userRepository.existsByUsername(userSignUpRequest.getUsername())) {
            throw new RuntimeException("SignUp failed: Username already exists");
        } else if (userRepository.existsByEmail(userSignUpRequest.getEmail())) {
            throw new RuntimeException("SignUp failed: Email already exists");
        } else if (userRepository.existsByPhoneNumber(userSignUpRequest.getPhoneNumber())) {
            throw new RuntimeException("SignUp failed: Phone number already exists");
        }
        try {
            User user = User.builder()
                    .username(userSignUpRequest.getUsername())
                    .password(passwordEncoder.encode(userSignUpRequest.getPassword()))
                    .fullname(userSignUpRequest.getFullname())
                    .email(userSignUpRequest.getEmail())
                    .phoneNumber(userSignUpRequest.getPhoneNumber())
                    .role(UserRole.valueOf(userSignUpRequest.getRole().trim()))
                    .isVerified(false)
                    .address(userSignUpRequest.getAddress())
                    .status(userSignUpRequest.isActive() ? UserStatus.ACTIVE : UserStatus.INACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            // Upload avatar
            if (userSignUpRequest.getAvatar() != null) {
                CompletableFuture<String> avatar = cloudinaryService.uploadFileAsync(userSignUpRequest.getAvatar());
                user.setAvatar(avatar.join());
            }

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
                user.setAvatar(userInfo.getPicture());
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
    public void update(UUID id, UpdateUserRequest updateUserRequest, MultipartFile avatar) {
        if (isValidToUpdate(id)) {
            throw new AuthorizationDeniedException("Access denied");
        }
        User user = userRepository.getReferenceById(id);
        String error = Validator.verifyUserUpdate(user, updateUserRequest);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        updateBasicInfo(user, updateUserRequest);
        if (avatar != null) {
            updateAvatar(user, avatar);
        }
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
            User userByPhoneNumber = userRepository.findByPhoneNumber(updateUserRequest.getPhoneNumber());
            if (userByPhoneNumber != null && !userByPhoneNumber.getId().equals(user.getId())) {
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

    private void updateAvatar(User user, MultipartFile avatar) {
        if (avatar != null) {
            String oldAvatar = user.getAvatar();
            try {
                CompletableFuture<String> newAvatar = cloudinaryService.uploadFileAsync(avatar);
                user.setAvatar(newAvatar.join());
                if (oldAvatar != null && !oldAvatar.isEmpty()) {
                    cloudinaryService.deleteImageAsync(oldAvatar);
                }
            } catch (Exception e) {
                log.error("Error updating avatar", e);
                throw new RuntimeException("Error updating avatar");
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
            if (Validator.isValidPassword(updatePasswordRequest.getNewPassword())) {
                throw new IllegalArgumentException("Invalid new password");
            }
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Invalid password");
        }
    }

    @Override
    public DeviceTokenDTO registerDeviceToken(UUID id, String token) {
        // Check valid of user id
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Check if the token already exists
        List<DeviceToken> deviceTokens = user.getDeviceTokens();
        for (DeviceToken deviceToken : deviceTokens) {
            if (deviceToken.getToken().equals(token)) {
                throw new IllegalArgumentException("Device token already exists.");
            }
        }
        // Create new device token
        DeviceToken newDeviceToken = DeviceToken.builder().token(token).user(user).build();
        user.getDeviceTokens().add(newDeviceToken);
        return DeviceTokenMapper.toDeviceTokenDTO(deviceTokenRepository.save(newDeviceToken));
    }

    @Override
    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
        user.setDeleted(true);
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
    public UserListDTO getUsers(int page, int size, String role, String status,
                                String search, Boolean isVerified, Boolean isDeleted,
                                String sortField, String sortDirection, LocalDateTime createdAtMin,
                                LocalDateTime createdAtMax, Boolean isOnline) {
        String errorMessage = Validator.verifyUserFilter(page, size, role, status, sortField, sortDirection, User.class);
        if (errorMessage != null) {
            throw new IllegalArgumentException(errorMessage);
        }

        Specification<User> spec = Specification.where(
                UserSpecification.hasRole(role)
                        .and(UserSpecification.hasStatus(status))
                        .and(UserSpecification.createdAtBetween(createdAtMin, createdAtMax))
                        .and(UserSpecification.isDeleted(isDeleted))
                        .and(UserSpecification.isVerified(isVerified))
        );

        if (search != null && !search.isEmpty()) {
            spec = spec.and(UserSpecification.search(search));
        }
        if (isOnline) {
            spec = spec.and(UserSpecification.hasNonExpiredToken());
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        return UserMapper.toUserListDTO(
                userPage.getContent(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getNumber(),
                userPage.getSize()
        );

    }

    private boolean isValidToUpdate(UUID id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Customer can only update their own password
        User authenticatedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return (authenticatedUser.getRole().equals(UserRole.ROLE_CUSTOMER) || authenticatedUser.getRole().equals(UserRole.ROLE_STAFF)) && authenticatedUser.getId().equals(id);
    }

    @Override
    public OrderListDTO getOrders(User user, String status, int page, int size) {
        if (Validator.isValidOrderStatus(status)) {
            throw new IllegalArgumentException("Invalid order status");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;
        if (status.equals("all")) {
            orderPage = orderRepository.findAllByUserId(user.getId(), pageable);
        }else {
            orderPage = orderRepository.findAllByUserIdAndStatus(user.getId(), status, pageable);
        }
        long totalAmount = orderPage.stream().mapToLong(order -> order.getTotalAmount().longValue()).sum();
        return OrderMapper.toOrderListDTO(
                orderPage.getContent(),
                totalAmount,
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getNumber(),
                orderPage.getSize()
        );
    }
}