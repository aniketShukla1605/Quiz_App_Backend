package com.microservice.authService.controller;

import com.microservice.authService.dto.GoogleLoginRequest;
import com.microservice.authService.dto.LoginRequest;
import com.microservice.authService.dto.RegisterRequest;
import com.microservice.authService.dto.ResendVerificationRequest;
import com.microservice.authService.service.AuthService;
import com.microservice.authService.service.EmailVerificationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest googleLoginRequest,
                                         HttpServletResponse response) {
        return authService.googleLogin(googleLoginRequest, response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        return emailVerificationService.verifyEmail(token);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return emailVerificationService.resendVerification(request.getEmail());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {

        String refreshToken = getCookieValue(request, "refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("No refresh token found");
        }

        return authService.logout(refreshToken, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {

        String refreshToken = getCookieValue(request, "refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("No refresh token found");
        }

        return authService.refresh(refreshToken, response);
    }

    private String getCookieValue(HttpServletRequest request, String name) {

        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }

        return null;
    }
}