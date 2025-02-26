package com.example.electrical_preorder_system_backend.config.jwt;

import com.example.electrical_preorder_system_backend.config.utils.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            //Parse jwt token
            String jwt = parseJwt(request);
            //If jwt token is not null and valid
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                UserDetails userDetails = null;
                String subject = jwtUtils.getSubjectFromToken(jwt);
                UsernamePasswordAuthenticationToken authenticationToken = null;
                if ("google".equals(subject)) {
                    // If access with Google account
                    String email = jwtUtils.getSubjectFromToken(jwt);
                    userDetails = userDetailsService.loadUserByEmail(email);
                } else {
                    // If access with normal account
                    String username = jwtUtils.getSubjectFromToken(jwt);
                    userDetails = userDetailsService.loadUserByUsername(username);
                }
                authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            log.error("Can not set user authentication: ", e);
        }
        filterChain.doFilter(request, response);
    }
}
