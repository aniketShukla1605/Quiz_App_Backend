package com.microservice.quiz.controller;

import com.microservice.quiz.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microservice.quiz.dto.AttemptResultResponse;

import java.util.List;
import java.util.UUID;

//controller made for result service
@RestController
@RequestMapping("/quiz/internal/attempts")
@RequiredArgsConstructor
public class QuizAttemptInternalController {

    private final QuizAttemptService attemptService;

    @GetMapping("/{attemptId}")
    public ResponseEntity<AttemptResultResponse> getAttemptResult(@PathVariable UUID attemptId) {
        return attemptService.getAttemptResult(attemptId);
    }

    @GetMapping
    public ResponseEntity<List<AttemptResultResponse>> getStudentAttemptResults(@RequestParam UUID studentId) {
        return attemptService.getStudentAttemptResults(studentId);
    }
}
