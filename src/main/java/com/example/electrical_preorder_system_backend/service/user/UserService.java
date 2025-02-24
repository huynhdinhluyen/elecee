package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.config.utils.UserDetailsImpl;
import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.request.UserSignUpRequest;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.enums.UserStatus;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import com.example.electrical_preorder_system_backend.service.email.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User signUp(UserSignUpRequest userSignInRequest) throws MessagingException {
        try{
            if (userRepository.existsByUsername(userSignInRequest.getUsername())) {
                throw new RuntimeException("SignUp failed: Username already exists");
            }else if (userRepository.existsByEmail(userSignInRequest.getEmail())) {
                throw new RuntimeException("SignUp failed: Email already exists");
            }else if (userRepository.existsByPhoneNumber(userSignInRequest.getPhoneNumber())){
                throw new RuntimeException("SignUp failed: Phone number already exists");
            }
            String role = userSignInRequest.getRole();
            if (!isValidRole(role)) {
                throw new RuntimeException("SignUp failed: Invalid role");
            }

            User user =  User.builder()
                    .username(userSignInRequest.getUsername())
                    .password(passwordEncoder.encode(userSignInRequest.getPassword()))
                    .fullname(userSignInRequest.getFullname())
                    .email(userSignInRequest.getEmail())
                    .phoneNumber(userSignInRequest.getPhoneNumber())
                    .role(UserRole.valueOf(role))
                    .isVerified(true)
                    .status(userSignInRequest.isActive()? UserStatus.ACTIVE: UserStatus.INACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            String jwtToken = jwtUtils.generateToken(user,"normal");
            user.setToken(jwtToken);
            user.setTokenExpires(LocalDateTime.ofInstant(jwtUtils.getExpDateFromToken(jwtToken).toInstant(), ZoneId.systemDefault()));
            return userRepository.save(user);
        }catch(TransactionSystemException ex) {
            log.error("Transaction failed: ", ex);
            throw new RuntimeException("Sign-up failed due to transaction error", ex);
        }
    }

    @Override
    public String googeLogin(UserLoginRequest userLoginRequest) throws MessagingException {
        Optional<User> userOptional;
        //login with Google
        if (!userLoginRequest.getGoogleAccountId().isEmpty()){
            userOptional = userRepository.findByGoogleAccountId(userLoginRequest.getGoogleAccountId());
            String jwtToken = "";
            if (userOptional.isEmpty()){
                //register user
                User newUser = User.builder()
                        .googleAccountId(userLoginRequest.getGoogleAccountId())
                        .username(userLoginRequest.getUsername())
                        .fullname(userLoginRequest.getFullName())
                        .email(userLoginRequest.getUsername())
                        .role(UserRole.ROLE_CUSTOMER)
                        .isVerified(false)
                        .status(UserStatus.INACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                //Send email verification
                emailService.sendEmail(
                        userLoginRequest.getUsername(),
                        emailService.subjectRegister(),
                        emailService.bodyRegister(
                                userLoginRequest.getUsername(),
                                userLoginRequest.getUsername()
                        )
                );
                jwtToken = jwtUtils.generateToken(newUser,"google");
                newUser.setToken(jwtToken);
                newUser.setTokenExpires(LocalDateTime.ofInstant(jwtUtils.getExpDateFromToken(jwtToken).toInstant(), ZoneId.systemDefault()));
                userRepository.save(newUser);
            }else {
                User user = userOptional.get();
                jwtToken = jwtUtils.generateToken(user,"google");
            }
            log.info("Login with google - token: {}", jwtToken);
            return jwtToken;
        }else {
            // Login with username and password
            log.info("Login with username and password request: {}", userLoginRequest);
            try {
                Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(userLoginRequest.getUsername(), userLoginRequest.getPassword()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                return jwtUtils.generateToken(userDetails, "normal");
            } catch (Exception e) {
                throw new BadCredentialsException("Invalid username or password");
            }
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
