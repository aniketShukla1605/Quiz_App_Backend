package com.microservice.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class QuestionDto {
    private int id;
    private String questionTitle;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}
