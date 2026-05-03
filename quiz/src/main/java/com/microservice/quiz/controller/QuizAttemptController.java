package com.microservice.quiz.controller;

import com.microservice.quiz.dto.SubmitRequest;
import com.microservice.quiz.dto.SyncRequest;
import com.microservice.quiz.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

//Only accessible by STUDENT
@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService attemptService;

    //start
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startQuiz(
            @PathVariable int id,
            @RequestHeader("X-User-Id") UUID studentId) {
        return attemptService.startQuiz(id, studentId);
    }

    //sync
    @PostMapping("/{id}/sync")
    public ResponseEntity<?> syncQuiz(
            @PathVariable int id,
            @RequestHeader("X-User-Id") UUID studentId,
            @RequestBody SyncRequest request) {
        return attemptService.syncQuiz(id, studentId, request);
    }

    //submit
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitQuiz(
            @PathVariable int id,
            @RequestHeader("X-User-Id") UUID studentId,
            @RequestBody SubmitRequest request) {
        return attemptService.submitQuiz(id, studentId, request);
    }
}