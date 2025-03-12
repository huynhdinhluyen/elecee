package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UpdatePasswordRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateUserRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.*;
import com.example.electrical_preorder_system_backend.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    void update(UUID id, UpdateUserRequest updateUserRequest);

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

    /** Get all users with pageable, now available for admin only
     *
     * @param pageable Pageable
     * @return Page of User
     */
    UserListDTO getUsers(Pageable pageable);

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
