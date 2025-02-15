package com.example.electrical_preorder_system_backend.config.jwt;

import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    public String generateToken(User user) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        try {
            return Jwts.builder()
                    .subject(getSubject(user))
                    .issuer("electrical_preorder_system")
                    .claims(claims)
                    .signWith(getSecretKey(), Jwts.SIG.HS256)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .id(String.valueOf(UUID.randomUUID()))
                    .compact();
        } catch (Exception e) {
            log.error("Error generating token: {}", e.getMessage());
        }
        return null;
    }

    public String generateVerificationToken(String email){
        return Jwts.builder()
                .subject(email)
                .issuer("elecee")
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .issuedAt(new Date())
                .expiration(new Date(new Date().toInstant().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .id(String.valueOf(UUID.randomUUID()))
                .compact();
    }

    //If login with Google, the subject will be the email
    //If login with username and password, the subject will be the username
    public static String getSubject(User user) {
        String subject = user.getEmail();
        if (subject == null || subject.isEmpty()) {
            subject = user.getName();
        }
        return user.getEmail();
    }

    public Date getExpDateFromToken(String token){
        try {
            return Jwts
                    .parser()
                    .requireIssuer("elecee")
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload().getExpiration();
        } catch (Exception e) {
            log.error("Error getting expiration date from token: {}", e.getMessage());
        }
        return null;
    }

    public String getSubjectFromToken(String token) {
        try {
            return Jwts
                    .parser()
                    .requireIssuer("elecee")
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload().getSubject();
        } catch (Exception e) {
            log.error("Error getting subject from token: {}", e.getMessage());
        }
        return null;
    }

}
