package com.example.electrical_preorder_system_backend.config.jwt;

import com.example.electrical_preorder_system_backend.config.utils.UserDetailsImpl;
import com.example.electrical_preorder_system_backend.entity.User;
import com.example.electrical_preorder_system_backend.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class JwtUtils {
    private final UserRepository userRepository;
    @Value("${JWT_SECRET}")
    private String jwtSecret;
    @Value("${JWT_EXPIRATION}")
    private int jwtExpirationMs;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String jwtToken) {
        try {
            Jwts
                    .parser()
                    .requireIssuer("elecee")
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
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

    public String generateTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        User user = userRepository.findByUsername(username).get();
        claims.put("id", user.getId());
        claims.put("role", user.getRole());
        claims.put("fullName", user.getFullname());
        try {
            return Jwts.builder()
                    .subject(username)
                    .issuer("elecee")
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

    public String generateVerificationToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuer("elecee")
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .issuedAt(new Date())
                .expiration(new Date(new Date().toInstant().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .id(String.valueOf(UUID.randomUUID()))
                .compact();
    }

    public Date getExpDateFromToken(String token) {
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

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }
}
