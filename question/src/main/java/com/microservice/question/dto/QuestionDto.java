package com.microservice.question.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDto {
    private int id;
    private String questionTitle;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}
