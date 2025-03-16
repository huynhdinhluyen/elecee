package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.user.UpdatePasswordRequest;
import com.example.electrical_preorder_system_backend.dto.request.user.UpdateUserRequest;
import com.example.electrical_preorder_system_backend.dto.request.user.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.device_token.DeviceTokenDTO;
import com.example.electrical_preorder_system_backend.dto.response.order.OrderListDTO;
import com.example.electrical_preorder_system_backend.dto.response.user.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.dto.response.user.UserDTO;
import com.example.electrical_preorder_system_backend.dto.response.user.UserListDTO;
import com.example.electrical_preorder_system_backend.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IUserService {

    /** Sign up account with UserSignUpRequest, now available for staff only
     * Send email verification to user's email after sign up
     *
     * @param userSignInRequest UserSignUpRequest
     * @return UserDTO
     * @throws MessagingException MessagingException
     */
    UserDTO signUp(UserSignUpRequest userSignInRequest) throws MessagingException;

    /** Login with Google account with code from Google
     *
     * @param code code from Google
     * @return AuthenticationResponse containing JWT token
     * @throws MessagingException MessagingException
     */
    AuthenticationResponse googleLogin(String code) throws MessagingException;

    /** Verify email with token
     *
     * @param token JWT token from email
     */
    void verifyEmail(String token);

    /** Update user information with UpdateUserRequest
     *
     * @param id UUID
     * @param updateUserRequest UpdateUserRequest
     */
    void update(UUID id, UpdateUserRequest updateUserRequest, MultipartFile avatar);

    /** Delete user by id
     *
     * @param id UUID
     */
    void delete(UUID id);

    /** Get user information by id
     *
     * @param id UUID User's id
     * @return UserDTO
     */
    UserDTO getById(UUID id);

    /** Get all users with filter
     *
     * @return UserListDTO
     */
    UserListDTO getUsers(int page, int size, String role, String status,
                         String search, Boolean isVerified, Boolean isDeleted,
                         String sortField, String sortDirection, LocalDateTime createdAtMin,
                         LocalDateTime createdAtMax, Boolean isOnline);

    /** Update password with current password and new password
     *
     * @param id UUID User's id
     * @param updatePasswordRequest UpdatePasswordRequest
     */
    void updatePassword(UUID id, UpdatePasswordRequest updatePasswordRequest);

    /** Register device token for push notification
     * If device token already exists, throw exception
     *
     * @param id UUID User's id
     * @param token String Device token from FCM
     * @return DeviceTokenDTO
     */
    DeviceTokenDTO registerDeviceToken(UUID id, String token);

    /** Get authenticated user from SecurityContextHolder
     *
     * @return User
     */
    User getAuthenticatedUser();

    /** Get user by id
     *
     * @param id UUID User's id
     * @return User
     */
    User getUserById(UUID id);

    /** Get orders by user and status
     *
     * @param user Authenticated user
     * @param status String Order status
     * @param page int Page number
     * @param size int Page size
     * @return OrderListDTO
     */
    OrderListDTO getOrders(User user, String status, int page, int size);
}
