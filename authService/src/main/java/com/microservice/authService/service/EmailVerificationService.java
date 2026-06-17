package com.microservice.authService.service;

import com.microservice.authService.entity.EmailVerificationToken;
import com.microservice.authService.entity.User;
import com.microservice.authService.repository.EmailVerificationTokenRepository;
import com.microservice.authService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailVerificationTokenWriter tokenWriter;

    public void issueVerificationToken(User user) {
        tokenWriter.issueVerificationToken(user);
    }

    @Transactional
    public ResponseEntity<?> verifyEmail(String tokenIdRaw) {
        UUID tokenId;
        try {
            tokenId = UUID.fromString(tokenIdRaw);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid verification token");
        }

        EmailVerificationToken token = tokenRepository.findByTokenIdAndIsUsed(tokenId, false)
                .orElse(null);

        if (token == null) {
            return ResponseEntity.badRequest().body("Invalid or already-used verification link");
        }

        if (token.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(410).body("Verification link has expired. Please request a new one.");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        return ResponseEntity.ok("Email verified successfully");
    }

    public ResponseEntity<?> resendVerification(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.isEmailVerified()) {
            return ResponseEntity.ok("If an account with that email exists and isn't verified, a new link has been sent.");
        }
        tokenWriter.issueVerificationToken(user);
        return ResponseEntity.ok("If an account with that email exists and isn't verified, a new link has been sent.");
    }
}