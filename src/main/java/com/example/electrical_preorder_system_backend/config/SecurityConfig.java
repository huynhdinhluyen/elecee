package com.example.electrical_preorder_system_backend.config;

import com.example.electrical_preorder_system_backend.config.jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint JwtUnauthorizedHandler;

    private static final List<String> SECURED_URLS =
            List.of("user/p");

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//        // disable Cross-Site Request Forgery and state in session
//        httpSecurity.csrf(AbstractHttpConfigurer::disable)
//                //handle unauthorized access attempts
//                .exceptionHandling(exception -> exception.authenticationEntryPoint(JwtUnauthorizedHandler))
//                //stateless session
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                //authorize requests
//                .authorizeHttpRequests(auth ->
//                        auth
//                                //permit all requests to the following URLs
//                                .requestMatchers(SECURED_URLS.toArray(String[]::new)).authenticated()
//                                //permit all other requests
//                                .anyRequest().permitAll()
//                )
//                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(request -> {
//                    var cors = new CorsConfiguration();
//                    cors.setAllowedOrigins(List.of("*"));
//                    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
//                    cors.setAllowedHeaders(List.of("*"));
//                    return cors;
//                }))
//                .oauth2Login(Customizer.withDefaults())
//                .formLogin(Customizer.withDefaults())
//                ;
        httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());


        return httpSecurity.build();
    }

}
