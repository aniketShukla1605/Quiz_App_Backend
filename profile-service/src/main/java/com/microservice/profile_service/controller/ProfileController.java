package com.microservice.profile_service.controller;

import com.microservice.profile_service.dto.*;
import com.microservice.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email) {
        return profileService.getOrCreateProfile(UUID.fromString(userId), email);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(UUID.fromString(userId), email, request);
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<QuizHistoryResponse>> getMyHistory(
            @RequestHeader("X-User-Id") String userId) {
        return profileService.getHistory(UUID.fromString(userId));
    }

    // Internal endpoint
    @PostMapping("/internal/history")
    public ResponseEntity<Void> recordQuizResult(
            @RequestBody QuizResultEvent event) {
        return profileService.recordQuizResult(event);
    }
}