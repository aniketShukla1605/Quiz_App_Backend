package com.microservice.profile_service.service;

import com.microservice.profile_service.dto.*;
import com.microservice.profile_service.entity.QuizHistory;
import com.microservice.profile_service.entity.UserProfile;
import com.microservice.profile_service.repository.QuizHistoryRepository;
import com.microservice.profile_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final QuizHistoryRepository quizHistoryRepository;

    // Lazy profile creation — profile is created on first access
    public ResponseEntity<ProfileResponse> getOrCreateProfile(UUID userId, String email) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> createDefaultProfile(userId, email));

        return ResponseEntity.ok(toProfileResponse(profile));
    }

    public ResponseEntity<ProfileResponse> updateProfile(UUID userId, String email, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> createDefaultProfile(userId, email));

        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        return ResponseEntity.ok(toProfileResponse(profile));
    }

    public ResponseEntity<List<QuizHistoryResponse>> getHistory(UUID userId) {
        List<QuizHistory> history = quizHistoryRepository
                .findByUserIdOrderByAttemptedAtDesc(userId);

        List<QuizHistoryResponse> response = history.stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Void> recordQuizResult(QuizResultEvent event) {
        QuizHistory history = QuizHistory.builder()
                .userId(UUID.fromString(event.getUserId()))
                .quizId(event.getQuizId())
                .score(event.getScore())
                .totalQuestions(event.getTotalQuestions())
                .attemptedAt(LocalDateTime.now())
                .build();

        quizHistoryRepository.save(history);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //helpers
    private UserProfile createDefaultProfile(UUID userId, String email) {
        String defaultName = email != null && email.contains("@")
                ? email.substring(0, email.indexOf("@"))
                : "User";

        UserProfile profile = UserProfile.builder()
                .id(userId)
                .displayName(defaultName)
                .avatarUrl(null)
                .bio(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userProfileRepository.save(profile);
    }

    private ProfileResponse toProfileResponse(UserProfile profile) {
        return ProfileResponse.builder()
                .userId(profile.getId())
                .displayName(profile.getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private QuizHistoryResponse toHistoryResponse(QuizHistory history) {
        String percentage = history.getTotalQuestions() > 0
                ? String.format("%d/%d (%.0f%%)",
                history.getScore(),
                history.getTotalQuestions(),
                (history.getScore() * 100.0) / history.getTotalQuestions())
                : "N/A";

        return QuizHistoryResponse.builder()
                .id(history.getId())
                .quizId(history.getQuizId())
                .score(history.getScore())
                .totalQuestions(history.getTotalQuestions())
                .resultPercentage(percentage)
                .attemptedAt(history.getAttemptedAt())
                .build();
    }
}
