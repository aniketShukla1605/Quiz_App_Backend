package com.microservice.quiz.controller;

import com.microservice.quiz.dto.QuizInfoResponse;
import com.microservice.quiz.repository.QuizRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quiz/internal")
@RequiredArgsConstructor
public class QuizInternalController {

    private final QuizRepo quizRepo;

    @GetMapping("/search")
    public ResponseEntity<List<QuizInfoResponse>> findByTitle(@RequestParam String title) {
        List<QuizInfoResponse> results = quizRepo.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(q -> new QuizInfoResponse(q.getId(), q.getTitle(), q.getCategory()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
}