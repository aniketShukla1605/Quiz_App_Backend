package com.microservice.result_service.feign;

import com.microservice.result_service.dto.QuizInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "quiz")
public interface QuizServiceClient {

    @GetMapping("/quiz/internal/search")
    ResponseEntity<List<QuizInfoResponse>> findByTitle(@RequestParam String title);
}