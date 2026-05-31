package com.microservice.result_service.controller;

import com.microservice.result_service.dto.RecordResultRequest;
import com.microservice.result_service.dto.ResultHistoryResponse;
import com.microservice.result_service.dto.ScoreSummaryResponse;
import com.microservice.result_service.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.microservice.result_service.dto.LeaderboardEntryResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @GetMapping("/me/history")
    public ResponseEntity<List<ResultHistoryResponse>> getMyHistory(@RequestHeader("X-User-Id") UUID studentId) {
        return ResponseEntity.ok(resultService.getMyHistory(studentId));
    }

    @GetMapping("/me/summary")
    public ResponseEntity<ScoreSummaryResponse> getMyScoreSummary(@RequestHeader("X-User-Id") UUID studentId) {
        return ResponseEntity.ok(resultService.getScoreSummary(studentId));
    }

    @PostMapping("/me/sync")
    public ResponseEntity<List<ResultHistoryResponse>> syncMyResults(@RequestHeader("X-User-Id") UUID studentId) {
        return ResponseEntity.ok(resultService.syncMyResults(studentId));
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<ResultHistoryResponse> getAttemptResult(
            @PathVariable UUID attemptId,
            @RequestHeader("X-User-Id") UUID studentId) {
        return ResponseEntity.ok(resultService.getAttemptResult(attemptId, studentId));
    }

    @PostMapping("/internal/record")
    public ResponseEntity<Void> recordResult(@RequestBody RecordResultRequest request) {
        resultService.recordResult(request);
        return ResponseEntity.ok().build();
    }

    // Search leaderboard by quiz title
    @GetMapping("/quiz/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getQuizLeaderboard(
            @RequestParam String title,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(resultService.getQuizLeaderboard(title, limit));
    }
}