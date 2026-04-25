package com.microservice.quiz.controller;


import com.microservice.quiz.dto.QuestionDto;
import com.microservice.quiz.dto.QuizDto;
import com.microservice.quiz.dto.Response;
import com.microservice.quiz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
public class QuizController {
    //Required to create Quiz
    @Autowired
    private QuizService quizService;

    //Create a Quiz with random Questions
    @PostMapping("/create")
    public ResponseEntity<String> createQuiz(@RequestBody QuizDto quizDto){
        return quizService.createQuiz(quizDto.getCategory(), quizDto.getNumOfQ(), quizDto.getTitle());
    }

    //Get all the questions of a quiz
    @GetMapping("/get/{id}")
    public ResponseEntity<List<QuestionDto>> getQuizQuestions(@PathVariable int id){
        return quizService.getQuizQuestions(id);
    }

    //submit a quiz
    @PostMapping("/submit/{id}")
    public ResponseEntity<Integer> submitQuiz(@PathVariable int id, @RequestBody List<Response> responses){
        return quizService.calculateResult(id,responses);
    }
}
