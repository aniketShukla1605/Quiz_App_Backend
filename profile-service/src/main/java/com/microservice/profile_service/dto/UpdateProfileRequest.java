package com.microservice.profile_service.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String displayName;
    private String avatarUrl;
    private String bio;
}