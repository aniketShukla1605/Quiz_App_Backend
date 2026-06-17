package com.microservice.authService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;
}