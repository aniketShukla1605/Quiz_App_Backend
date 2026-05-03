package com.microservice.profile_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    private UUID id;           // same ID as authService User

    private String displayName;
    private String avatarUrl;
    private String bio;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
