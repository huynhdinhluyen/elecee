package com.example.electrical_preorder_system_backend.service.user;

import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.config.utils.UserDetailsImpl;
import com.example.electrical_preorder_system_backend.dto.request.UserLoginRequest;
import com.example.electrical_preorder_system_backend.dto.response.AuthenticationResponse;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.enums.UserRole;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Override
    public AuthenticationResponse login(UserLoginRequest userLoginRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userLoginRequest.getUsername(), userLoginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (user.getRole().equals(UserRole.ROLE_STAFF) && !user.isVerified()) {
                throw new RuntimeException("Login failed. Please verify your email first");
            }
            String token = jwtUtils.generateJwtToken(userDetails);
            return new AuthenticationResponse(token);
        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("User not found");
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String generateAuthUrl(String loginType) {
        String url = "";
        if (loginType.equals("google")) {
            url = "https://accounts.google.com/o/oauth2/auth?"
                    + "client_id=" + clientId
                    + "&redirect_uri=" + redirectUri
                    + "&response_type=code&scope=email%20profile";
        }
        return url;
    }
}
