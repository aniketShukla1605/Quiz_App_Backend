package com.microservice.question.controller;

import com.microservice.question.dto.QuestionDto;
import com.microservice.question.dto.Response;
import com.microservice.question.model.Question;
import com.microservice.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    //All Questions
    @GetMapping("/allQuestions")
    public ResponseEntity<List<Question>> allQuestions() {
        return questionService.getAllQuestions();
    }

    //Only for Specific category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Question>> getQuestionsByCategory(@PathVariable String category) {
        return questionService.getQuestionsByCategory(category);
    }

    //Add a Question
    @PostMapping("/add")
    public String addQuestion(@RequestBody Question question) {
        return questionService.addQuestion(question);
    }

    //Generate Quiz
    @GetMapping("/generate")
    public ResponseEntity<List<Integer>> generateQuestions(@RequestParam String category, @RequestParam int numberOfQuestions) {
        return questionService.generateQuestions(category,numberOfQuestions);
    }

    //Get Questions
    @PostMapping("getQuestions")
    public ResponseEntity<List<QuestionDto>> getQuestionsFromId(@RequestBody List<Integer> ids) {
        return questionService.getQuetionsFromId(ids);
    }

    //Get Score
    @PostMapping("getScore")
    public ResponseEntity<Integer> getScore(@RequestBody List<Response> responses) {
        return questionService.getScore(responses);
    }
}
