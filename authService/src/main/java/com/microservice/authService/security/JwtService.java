package com.microservice.authService.security;

import com.microservice.authService.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${jwt.service}")
    private String secret;

    public SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(UUID userId, String email, Role role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email",email)
                .claim("role",role.name())
                .claim("type","access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*25))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(UUID userId, UUID tokenId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type","refresh")
                .claim("tokenId",tokenId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*24*7))
                .signWith(getKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }
}
