package com.ridesharing.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate token with email as subject
    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)               // email is principal
                .claim("userId", userId)         // store numeric id as claim
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getEmail(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
        return claims.get("userId", Long.class);
    }

    public String getRole(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
        return claims.get("role", String.class);
    }
}
