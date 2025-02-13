package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.dto.request.UserRegisterRequest;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class UserService implements IUserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerUser(UserRegisterRequest userRegisterRequest) {
        //check if email already exists
        if (userRepository.findByEmail(userRegisterRequest.getEmail()).isPresent()) {
            log.error("Email already exists");
            throw new RuntimeException("Email already exists");
        }//check if phone number already exists
        else if (userRepository.findByPhoneNumber(userRegisterRequest.getPhoneNumber()).isPresent()) {
            log.error("Phone number already exists");
            throw new RuntimeException("Phone number already exists");
        }

        //create user
        User user = new User();
        user.setName(userRegisterRequest.getName());
        user.setEmail(userRegisterRequest.getEmail());
        user.setPhoneNumber(userRegisterRequest.getPhoneNumber());
        user.setStatus(UserStatus.INACTIVE);
        user.setRole(UserRole.CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        //endocde password
        user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));

        //save user
        userRepository.save(user);
        return new UserDTO();



    }

    public String generateVerificationToken(String username){
        return Jwts.builder()
                .subject(username)
                .issuer("electrical_preorder_system")
                .issuedAt(new Date())
                .expiration(new Date(new Date().toInstant().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .id(String.valueOf(UUID.randomUUID()))
                .compact();
    }

    @Override
    public UserDTO verifyAccount(String token) {
        return null;
    }
}
