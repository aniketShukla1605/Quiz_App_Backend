package com.microservice.authService.dto;

import com.microservice.authService.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "Google ID token is required")
    private String idToken;

    private Role role;
}
