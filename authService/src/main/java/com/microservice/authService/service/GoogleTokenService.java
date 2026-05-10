package com.microservice.authService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleTokenService {
    private static final List<String> GOOGLE_ISSUERS = List.of("https://accounts.google.com", "accounts.google.com");

    @Value("${google.oauth.client-id:}")
    private String clientId;

    @Value("${google.oauth.jwk-set-uri:https://www.googleapis.com/oauth2/v3/certs}")
    private String jwkSetUri;

    public GoogleUserInfo verify(String idToken) {
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("Google OAuth client ID is not configured");
        }

        JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        Jwt jwt = jwtDecoder.decode(idToken);

        if (!GOOGLE_ISSUERS.contains(jwt.getIssuer().toString())) {
            throw new IllegalArgumentException("Invalid Google token issuer");
        }

        if (!jwt.getAudience().contains(clientId)) {
            throw new IllegalArgumentException("Invalid Google token audience");
        }

        Boolean emailVerified = jwt.getClaim("email_verified");
        String email = jwt.getClaimAsString("email");

        if (!Boolean.TRUE.equals(emailVerified) || !StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Google email is not verified");
        }

        return new GoogleUserInfo(jwt.getSubject(), email);
    }

    public record GoogleUserInfo(String subject, String email) {
    }
}
