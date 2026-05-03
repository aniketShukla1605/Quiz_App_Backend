package com.microservice.profile_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResultEvent {
    private String userId;
    private Integer quizId;
    private Integer score;
    private Integer totalQuestions;
}