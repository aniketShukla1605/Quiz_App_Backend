package com.microservice.question.dto;

import com.microservice.question.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuizWithQuestionsDto {
    private String quizTitle;
    private String category;
    private List<Question> questions; // full question objects from teacher
}
