package com.microservice.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizDto {
    private String category;
    private int numOfQ;
    private String title;
    private Integer duration;
}
