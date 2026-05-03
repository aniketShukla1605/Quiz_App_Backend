package com.microservice.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StartQuizResponse {
    private UUID attemptId;
    private int quizId;
    private String state;
    private LocalDateTime startTime;
    private int durationMinutes;
    private LocalDateTime serverTime;
    private LocalDateTime expiryTime;
    private List<QuestionDto> questions;
}
