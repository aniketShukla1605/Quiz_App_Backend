package com.microservice.result_service.dto;

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
public class ResultHistoryResponse {
    private Long id;
    private UUID attemptId;
    private Integer quizId;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private String resultText;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private String submissionMethod;
}
