package com.microservice.quiz.feign;

import com.microservice.quiz.dto.QuestionDto;
import com.microservice.quiz.dto.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("QUESTION")
public interface QuizInterface {
    //Generate Quiz
    @GetMapping("/question/generate")
    public ResponseEntity<List<Integer>> generateQuestions(@RequestParam String category, @RequestParam int numberOfQuestions);

    //Get Questions
    @PostMapping("/question/getQuestions")
    public ResponseEntity<List<QuestionDto>> getQuestionsFromId(@RequestBody List<Integer> ids);

    //Get Score
    @PostMapping("/question/getScore")
    public ResponseEntity<Integer> getScore(@RequestBody List<Response> responses);
}
