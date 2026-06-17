package com.microservice.authService.service;

import com.microservice.authService.entity.EmailVerificationToken;
import com.microservice.authService.entity.User;
import com.microservice.authService.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenWriter {

    private static final int TOKEN_VALIDITY_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public void issueVerificationToken(User user) {
        tokenRepository.invalidateAllForUser(user);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenId(UUID.randomUUID())
                .user(user)
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS))
                .build();

        tokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getTokenId().toString());
    }
}