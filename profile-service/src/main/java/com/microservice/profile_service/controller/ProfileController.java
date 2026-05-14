package com.microservice.profile_service.controller;

import com.microservice.profile_service.dto.*;
import com.microservice.profile_service.service.CloudinaryService;
import com.microservice.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final CloudinaryService cloudinaryService;


    private static final String DEFAULT_AVATAR = "https://res.cloudinary.com/dolfdzaf2/image/upload/v1778251943/user_w6ziip.png";

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

    @PostMapping("/me/avatar")
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        String avatarUrl = cloudinaryService.uploadAvatar(file, userId);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setAvatarUrl(avatarUrl);

        return profileService.updateProfile(UUID.fromString(userId), email, request);
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<ProfileResponse> deleteAvatar(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email) {

        cloudinaryService.deleteAvatar(userId);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setAvatarUrl(DEFAULT_AVATAR);

        return profileService.updateProfile(
                UUID.fromString(userId),
                email,
                request
        );
    }

    // Internal endpoint
    @PostMapping("/internal/history")
    public ResponseEntity<Void> recordQuizResult(
            @RequestBody QuizResultEvent event) {
        return profileService.recordQuizResult(event);
    }
}