package com.microservice.quiz.controller;


import com.microservice.quiz.dto.CustomQuizDto;
import com.microservice.quiz.dto.QuestionDto;
import com.microservice.quiz.dto.QuizDto;
import com.microservice.quiz.dto.Response;
import com.microservice.quiz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
public class QuizController {
    //Required to create Quiz
    @Autowired
    private QuizService quizService;



    //Create a Quiz with random Questions only for TEACHER
    @PostMapping("/create")
    public ResponseEntity<String> createQuiz(@RequestBody QuizDto quizDto){
        return new ResponseEntity<>(quizService.createQuiz(quizDto.getCategory(), quizDto.getNumOfQ(), quizDto.getTitle(), quizDto.getDuration()), HttpStatus.CREATED);
    }

    //Get all the questions of a quiz
    @GetMapping("/get/{id}")
    public ResponseEntity<List<QuestionDto>> getQuizQuestions(@PathVariable int id){
        return ResponseEntity.ok(quizService.getQuizQuestions(id));
    }

    //only TEACHER role can reach this (enforced at gateway)
    @PostMapping("/createCustom")
    public ResponseEntity<String> createCustomQuiz(@RequestBody CustomQuizDto customQuizDto) {
        return new ResponseEntity(quizService.createCustomQuiz(customQuizDto), HttpStatus.CREATED);
    }

    //only STUDENT role can access this
    //No longer in use (replaced by QuizAttempt submit controller)
    @PostMapping("/submit/{id}")
    public ResponseEntity<Integer> submitQuiz(
            @PathVariable int id,
            @RequestBody List<Response> responses,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(quizService.calculateResult(id, responses, userId));
    }
}
