package com.microservice.quiz.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String category;
    //A Quiz can have many Questions and a Question can be in many Quizzes
    @ElementCollection
    private List<Integer> questionId;
}
