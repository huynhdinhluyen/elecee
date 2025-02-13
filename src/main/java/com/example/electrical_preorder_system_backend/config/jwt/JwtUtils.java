package com.example.electrical_preorder_system_backend.config.jwt;

import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION}")
    private int jwtExpirationMs;

    @Autowired
    private UserRepository userRepository;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String jwtToken) {
        try {
            SecretKey key = getSecretKey();
            Jwts.parser().decryptWith(key).build().parse(jwtToken);
            return true;
        }catch (SecurityException | MalformedJwtException e){
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getEmailFromToken(String jwtToken) {
        SecretKey key = getSecretKey();
        return Jwts.parser().decryptWith(key).build().parseSignedClaims(jwtToken).getPayload().getSubject();
    }

    public String generateToken(String email) {
        SecretKey key = getSecretKey();
        User user = userRepository.findByEmail(email).orElseThrow();
        return Jwts.builder()
                .subject(email)
                .issuer("electrical_preorder_system")
                .claim("userId", user.getId())
                .claim("username", user.getName())
                .claim("role", user.getRole())
                .signWith(key)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .id(String.valueOf(UUID.randomUUID()))
                .compact();
    }



}
