package com.microservice.authService.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:${spring.mail.username:no-reply@quizapp.local}}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${frontend.url:${FRONTEND_URL:http://localhost:5173}}")
    private String frontendUrl;

    EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private boolean isMailConfigured() {
        return StringUtils.hasText(mailUsername);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = frontendUrl + "/verify-email?token=" + token;

        if (!isMailConfigured()) {
            log.info("[EmailService] SMTP not configured. Verification link for {}: {}", toEmail, verifyLink);
            return;
        }

        try {
            String sender = StringUtils.hasText(mailUsername) ? mailUsername : fromAddress;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(toEmail);
            helper.setSubject("Verify your QuizApp email address");
            helper.setText(buildVerificationHtml(verifyLink), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}. Verification link: {}", toEmail, e.getMessage(), verifyLink);
        }
    }

    private String buildVerificationHtml(String verifyLink) {
        return "<div style=\"font-family:sans-serif;max-width:480px;margin:0 auto\">"
                + "<h2>Welcome to QuizApp!</h2>"
                + "<p>Please verify your email address to activate your account.</p>"
                + "<p><a href=\"" + verifyLink + "\" style=\"display:inline-block;padding:10px 20px;"
                + "background:#7c5cfc;color:#fff;border-radius:8px;text-decoration:none\">Verify Email</a></p>"
                + "<p>Or copy this link into your browser:<br>" + verifyLink + "</p>"
                + "<p>This link expires in 24 hours.</p>"
                + "</div>";
    }
}