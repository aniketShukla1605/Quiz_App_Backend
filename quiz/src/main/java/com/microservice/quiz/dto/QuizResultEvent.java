package com.microservice.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizResultEvent {
    private String userId;
    private Integer quizId;
    private Integer score;
    private Integer totalQuestions;
}
