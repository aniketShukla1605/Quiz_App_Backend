package com.microservice.question.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomQuizDto {
    private String title;
    private String category;
    private List<Integer> questionIds;
    private Integer duration;

    public CustomQuizDto(String title, String category, List<Integer> questionIds) {
        this.title = title;
        this.category = category;
        this.questionIds = questionIds;
    }
}