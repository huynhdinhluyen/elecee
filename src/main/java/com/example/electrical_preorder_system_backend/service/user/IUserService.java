package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UpdateUserRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import com.example.electrical_preorder_system_backend.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {

    UserDTO signUp(UserSignUpRequest userSignInRequest) throws MessagingException;

    AuthenticationResponse googleLogin(String code) throws MessagingException;

    void verifyEmail(String token);

    void update(UUID id, UpdateUserRequest updateUserRequest);

    void delete(UUID id);

    UserDTO getById(UUID id);

    Page<User> getUsers(Pageable pageable);
}
