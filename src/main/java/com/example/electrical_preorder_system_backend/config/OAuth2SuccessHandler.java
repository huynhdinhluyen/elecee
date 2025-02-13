package com.example.electrical_preorder_system_backend.config;

import com.example.electrical_preorder_system_backend.config.jwt.JwtUtils;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.sendRedirect("http://localhost:3000");

//        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
//        String email = oauthUser.getAttribute("email");
//        String username = oauthUser.getAttribute("name");
//
//        String token = jwtUtil.generateToken(username, email);
//
//        // Save to database
//        UserEntity user = userRepository.findByEmail(email).orElse(new UserEntity());
//        user.setEmail(email);
//        user.setUsername(username);
//        user.setJwtToken(token);
//        userRepository.save(user);
//
//        // Return token
//        response.sendRedirect("/swagger-ui/index.html?token=" + token);
        OAuth2
    }
}
