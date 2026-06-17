package com.microservice.authService.service;

import com.microservice.authService.dto.AuthResponse;
import com.microservice.authService.dto.GoogleLoginRequest;
import com.microservice.authService.dto.LoginRequest;
import com.microservice.authService.dto.RegisterRequest;
import com.microservice.authService.entity.RefreshToken;
import com.microservice.authService.entity.Role;
import com.microservice.authService.entity.User;
import com.microservice.authService.repository.RefreshTokenRepository;
import com.microservice.authService.repository.UserRepository;
import com.microservice.authService.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenService googleTokenService;
    private final EmailVerificationService emailVerificationService;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public ResponseEntity<?> register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        if(!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return ResponseEntity.badRequest().body("Password must contain uppercase,lowercase and at least 8 characters");
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        try {
            emailVerificationService.issueVerificationToken(user);
        } catch (Exception ignored) {}

        return ResponseEntity.status(201).body("Registration successful. Please check your email to verify your account.");
    }

    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse  response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if(user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        if(!user.isEmailVerified()) {
            return ResponseEntity.status(403).body("Email not verified. Please check your inbox or request a new verification link.");
        }
        return issueLoginResponse(user, response);
    }

    public ResponseEntity<?> googleLogin(GoogleLoginRequest request, HttpServletResponse response) {
        try {
            GoogleTokenService.GoogleUserInfo googleUser = googleTokenService.verify(request.getIdToken());

            User user = userRepository.findByGoogleSubject(googleUser.subject())
                    .or(() -> userRepository.findByEmail(googleUser.email()))
                    .map(existingUser -> {
                        boolean changed = false;
                        if (existingUser.getGoogleSubject() == null) {
                            existingUser.setGoogleSubject(googleUser.subject());
                            changed = true;
                        }
                        if (!existingUser.isEmailVerified()) {
                            existingUser.setEmailVerified(true);
                            changed = true;
                        }
                        if (changed) {
                            existingUser.setUpdatedAt(LocalDateTime.now());
                            return userRepository.save(existingUser);
                        }
                        return existingUser;
                    })
                    .orElseGet(() -> {
                        Role role = request.getRole() == null ? Role.STUDENT : request.getRole();

                        User newUser = User.builder()
                                .email(googleUser.email())
                                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .googleSubject(googleUser.subject())
                                .role(role)
                                .isEmailVerified(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        return userRepository.save(newUser);
                    });

            return issueLoginResponse(user, response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Google token");
        }
    }

    private ResponseEntity<?> issueLoginResponse(User user, HttpServletResponse response) {
        UUID tokenId = UUID.randomUUID();

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        String refreshTokenJwt = jwtService.generateRefreshToken(
                user.getId(),
                tokenId
        );

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .user(user)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);

        addCookie(response,"accessToken",accessToken,900);
        addCookie(response,"refreshToken",refreshTokenJwt,604800);


        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), user.getRole()));
    }

    public ResponseEntity<?> logout(String refreshToken, HttpServletResponse response) {

        try {
            Claims claims = jwtService.extractClaims(refreshToken);
            UUID tokenId = UUID.fromString(claims.get("tokenId", String.class));

            refreshTokenRepository.findById(tokenId).ifPresent(token -> {
                token.setActive(false);
                refreshTokenRepository.save(token);
            });

        } catch (Exception ignored) {}

        clearCookie(response, "accessToken");
        clearCookie(response, "refreshToken");

        return ResponseEntity.ok("Logged out successfully");
    }

    public ResponseEntity<?> refresh(String refreshTokenCookie,
                                     HttpServletResponse response) {

        try {
            Claims claims = jwtService.extractClaims(refreshTokenCookie);

            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                return ResponseEntity.status(401).body("Invalid token type");
            }

            UUID userId = UUID.fromString(claims.getSubject());
            UUID oldTokenId = UUID.fromString(claims.get("tokenId", String.class));

            RefreshToken oldToken = refreshTokenRepository
                    .findByTokenIdAndIsActive(oldTokenId,true)
                    .orElse(null);

            if (oldToken == null) {
                return ResponseEntity.status(401).body("Token inactive or not found");
            }

            oldToken.setActive(false);
            refreshTokenRepository.save(oldToken);

            User user = oldToken.getUser();

            UUID newTokenId = UUID.randomUUID();

            String newAccessToken = jwtService.generateAccessToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole()
            );

            String newRefreshToken = jwtService.generateRefreshToken(
                    user.getId(),
                    newTokenId
            );

            RefreshToken newToken = RefreshToken.builder()
                    .tokenId(newTokenId)
                    .user(user)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            refreshTokenRepository.save(newToken);

            addCookie(response, "accessToken", newAccessToken, 900);
            addCookie(response, "refreshToken", newRefreshToken, 604800);

            return ResponseEntity.ok(
                    new AuthResponse(user.getId(), user.getEmail(), user.getRole())
            );

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }
    }

    private void addCookie(HttpServletResponse response,
                           String name,
                           String value,
                           int maxAge) {

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}