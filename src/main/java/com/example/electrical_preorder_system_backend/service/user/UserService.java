package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.UserDTO;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import com.example.electrical_preorder_system_backend.service.email.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class UserService implements IUserService {

    @Autowired
    private final JwtUtils jwtUtils;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EmailService emailService;

    public UserService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    //Service to register staff and admin
    @Override
    public UserDTO registerUser(UserLoginRequest userLoginRequest) {
        return null;
    }

    @Override
    public String login(UserLoginRequest userLoginRequest) throws MessagingException {
        Optional<User> userOptional = Optional.empty();
        String subject = "";
        //login with Google
        if (userLoginRequest.getGoogleAccountId()!=null && this.isValidGoogleId(userLoginRequest)){
            userOptional = userRepository.findByGoogleAccountId(userLoginRequest.getGoogleAccountId());
            subject = "Google: "+userLoginRequest.getGoogleAccountId();
            if (userOptional.isEmpty()){
                //register user
                User newUser = User.builder()
                        .googleAccountId(userLoginRequest.getGoogleAccountId())
                        .name(userLoginRequest.getUsername())
                        .email(userLoginRequest.getEmail())
                        .role(UserRole.CUSTOMER)
                        .isVerified(false)
                        .status(UserStatus.INACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                //Send email verification
                emailService.sendEmail(
                        userLoginRequest.getEmail(),
                        emailService.subjectRegister(),
                        emailService.bodyRegister(
                                userLoginRequest.getEmail(),
                                userLoginRequest.getUsername()
                        )
                );
                newUser = userRepository.save(newUser);
                userOptional = Optional.of(newUser);
            }
            return jwtUtils.generateToken(userOptional.get());
        }else {
            return null;
        }

    }

    public String subjectRegister(){
        return "Verify your email";
    }


    @Override
    public Boolean isValidGoogleId(UserLoginRequest userLoginRequest) {
        return true;
    }

    @Override
    public void verifyEmail(String token) {
        Date expDate = jwtUtils.getExpDateFromToken(token);

        // Check if the token is not expired
        if (!expDate.before(new Date())){
            String email = jwtUtils.getSubjectFromToken(token);
            if (email!=null){
                //Activate user account and set verified to true
                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()){
                    User user = userOptional.get();
                    user.setStatus(UserStatus.ACTIVE);
                    user.setVerified(true);
                    userRepository.save(user);
                    log.info("User with email {} verified", email);
                }else {
                    throw new RuntimeException("User not found");
                }
            }else {
                throw new RuntimeException("Invalid token");
            }
        }else {
            throw new RuntimeException("Token expired");
        }


    }


}
