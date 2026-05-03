package com.microservice.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitResponse {
    private UUID attemptId;
    private int quizId;
    private String state;
    private Integer score;
    private Integer maxScore;
    private LocalDateTime submittedAt;
    private String submissionMethod;
}