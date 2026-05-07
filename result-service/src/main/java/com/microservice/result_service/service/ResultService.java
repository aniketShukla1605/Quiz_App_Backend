package com.microservice.result_service.service;

import com.microservice.result_service.dto.QuizAttemptResultResponse;
import com.microservice.result_service.dto.RecordResultRequest;
import com.microservice.result_service.dto.ResultHistoryResponse;
import com.microservice.result_service.dto.ScoreSummaryResponse;
import com.microservice.result_service.entity.ResultHistory;
import com.microservice.result_service.feign.QuizAttemptClient;
import com.microservice.result_service.repository.ResultHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultHistoryRepository resultHistoryRepository;
    private final QuizAttemptClient quizAttemptClient;

    public ResponseEntity<Void> recordResult(RecordResultRequest request) {
        upsertResult(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<List<ResultHistoryResponse>> getMyHistory(UUID studentId) {
        syncResultsFromQuiz(studentId);

        List<ResultHistoryResponse> history = resultHistoryRepository
                .findByStudentIdOrderBySubmittedAtDesc(studentId)
                .stream()
                .map(this::toHistoryResponse) //used method reference operator :: to use the method
                .toList();

        return ResponseEntity.ok(history);
    }

    public ResponseEntity<ResultHistoryResponse> getAttemptResult(UUID attemptId, UUID studentId) {
        ResultHistory result = resultHistoryRepository.findByAttemptId(attemptId)
                .orElseGet(() -> fetchAndStoreAttempt(attemptId));

        if (result == null || !result.getStudentId().equals(studentId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toHistoryResponse(result));
    }

    public ResponseEntity<ScoreSummaryResponse> getScoreSummary(UUID studentId) {
        syncResultsFromQuiz(studentId);

        List<ResultHistory> results = resultHistoryRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
        int totalScore = results.stream().map(ResultHistory::getScore).mapToInt(score -> score == null ? 0 : score).sum();
        int totalMaxScore = results.stream().map(ResultHistory::getMaxScore).mapToInt(max -> max == null ? 0 : max).sum();
        double averagePercentage = totalMaxScore == 0 ? 0.0 : (totalScore * 100.0) / totalMaxScore;

        Integer bestScore = results.stream()
                .map(ResultHistory::getScore)
                .filter(score -> score != null)
                .max(Integer::compareTo)
                .orElse(0);

        Double bestPercentage = results.stream()
                .map(ResultHistory::getPercentage)
                .filter(percentage -> percentage != null)
                .max(Double::compareTo)
                .orElse(0.0);

        return ResponseEntity.ok(ScoreSummaryResponse.builder()
                .quizzesAttempted(results.size())
                .totalScore(totalScore)
                .totalMaxScore(totalMaxScore)
                .averagePercentage(round(averagePercentage))
                .bestScore(bestScore)
                .bestPercentage(round(bestPercentage))
                .build());
    }

    public ResponseEntity<List<ResultHistoryResponse>> syncMyResults(UUID studentId) {
        syncResultsFromQuiz(studentId);
        return getMyHistory(studentId);
    }

    private void syncResultsFromQuiz(UUID studentId) {
        ResponseEntity<List<QuizAttemptResultResponse>> response = quizAttemptClient.getStudentAttemptResults(studentId);
        List<QuizAttemptResultResponse> attempts = response.getBody();
        if (attempts == null) {
            return;
        }

        attempts.forEach(attempt -> upsertResult(toRecordRequest(attempt)));
    }

    private ResultHistory fetchAndStoreAttempt(UUID attemptId) {
        ResponseEntity<QuizAttemptResultResponse> response = quizAttemptClient.getAttemptResult(attemptId);
        QuizAttemptResultResponse attempt = response.getBody();
        if (attempt == null) {
            return null;
        }
        return upsertResult(toRecordRequest(attempt));
    }

    private ResultHistory upsertResult(RecordResultRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ResultHistory result = resultHistoryRepository.findByAttemptId(request.getAttemptId())
                .orElseGet(() -> ResultHistory.builder()
                        .attemptId(request.getAttemptId())
                        .recordedAt(now)
                        .build());

        result.setQuizId(request.getQuizId());
        result.setStudentId(request.getStudentId());
        result.setState(request.getState());
        result.setScore(request.getScore());
        result.setMaxScore(request.getMaxScore());
        result.setPercentage(calculatePercentage(request.getScore(), request.getMaxScore()));
        result.setStartedAt(request.getStartedAt());
        result.setSubmittedAt(request.getSubmittedAt());
        result.setSubmissionMethod(request.getSubmissionMethod());
        result.setUpdatedAt(now);

        return resultHistoryRepository.save(result);
    }

    private RecordResultRequest toRecordRequest(QuizAttemptResultResponse attempt) {
        return RecordResultRequest.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .studentId(attempt.getStudentId())
                .state(attempt.getState())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .submissionMethod(attempt.getSubmissionMethod())
                .build();
    }

    private ResultHistoryResponse toHistoryResponse(ResultHistory result) {
        return ResultHistoryResponse.builder()
                .id(result.getId())
                .attemptId(result.getAttemptId())
                .quizId(result.getQuizId())
                .score(result.getScore())
                .maxScore(result.getMaxScore())
                .percentage(result.getPercentage())
                .resultText(formatResult(result)) //to show result in a proper format
                .startedAt(result.getStartedAt())
                .submittedAt(result.getSubmittedAt())
                .submissionMethod(result.getSubmissionMethod())
                .build();
    }

    //to find percentage
    private Double calculatePercentage(Integer score, Integer maxScore) {
        if (score == null || maxScore == null || maxScore == 0) {
            return 0.0;
        }
        return round((score * 100.0) / maxScore);
    }

    private String formatResult(ResultHistory result) {
        if (result.getScore() == null || result.getMaxScore() == null || result.getMaxScore() == 0) {
            return "N/A";
        }
        return result.getScore() + "/" + result.getMaxScore() + " (" + result.getPercentage() + "%)";
    }

    //to round up double value
    private Double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
