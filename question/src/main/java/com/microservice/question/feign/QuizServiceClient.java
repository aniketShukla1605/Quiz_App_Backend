package com.microservice.question.feign;

import com.microservice.question.dto.CustomQuizDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("QUIZ")
public interface QuizServiceClient {

    @PostMapping("/quiz/createCustom")
    ResponseEntity<String> createCustomQuiz(@RequestBody CustomQuizDto customQuizDto);
}