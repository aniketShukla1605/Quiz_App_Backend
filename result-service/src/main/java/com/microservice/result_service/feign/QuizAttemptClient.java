package com.microservice.result_service.feign;

import com.microservice.result_service.dto.QuizAttemptResultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "quiz")
public interface QuizAttemptClient {
    //methods from quiz service

    @GetMapping("/quiz/internal/attempts/{attemptId}")
    ResponseEntity<QuizAttemptResultResponse> getAttemptResult(@PathVariable UUID attemptId);

    @GetMapping("/quiz/internal/attempts")
    ResponseEntity<List<QuizAttemptResultResponse>> getStudentAttemptResults(@RequestParam UUID studentId);
}
