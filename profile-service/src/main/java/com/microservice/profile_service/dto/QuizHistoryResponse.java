package com.microservice.profile_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizHistoryResponse {
    private Long id;
    private Integer quizId;
    private Integer score;
    private Integer totalQuestions;
    private String resultPercentage;   // e.g. "8/10 (80%)"
    private LocalDateTime attemptedAt;
}
