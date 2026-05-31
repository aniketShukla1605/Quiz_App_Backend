package com.microservice.result_service.service;

import com.microservice.result_service.dto.LeaderboardEntryResponse;
import com.microservice.result_service.dto.QuizAttemptResultResponse;
import com.microservice.result_service.dto.QuizInfoResponse;
import com.microservice.result_service.dto.RecordResultRequest;
import com.microservice.result_service.dto.ResultHistoryResponse;
import com.microservice.result_service.dto.ScoreSummaryResponse;
import com.microservice.result_service.dto.UserDisplayNameResponse;
import com.microservice.result_service.entity.ResultHistory;
import com.microservice.result_service.feign.ProfileServiceClient;
import com.microservice.result_service.feign.QuizAttemptClient;
import com.microservice.result_service.feign.QuizServiceClient;
import com.microservice.result_service.repository.ResultHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultHistoryRepository resultHistoryRepository;
    private final QuizAttemptClient quizAttemptClient;
    private final QuizServiceClient quizServiceClient;
    private final ProfileServiceClient profileServiceClient;

    @Caching(evict = {
            @CacheEvict(value = "resultHistory", key = "#request.studentId"),
            @CacheEvict(value = "scoreSummary", key = "#request.studentId"),
            @CacheEvict(value = "attemptResult", key = "#request.attemptId")
    })
    public void recordResult(RecordResultRequest request) {
        upsertResult(request);
    }

    @Cacheable(value = "resultHistory", key = "#studentId")
    public List<ResultHistoryResponse> getMyHistory(UUID studentId) {
        syncResultsFromQuiz(studentId);

        List<ResultHistoryResponse> history = resultHistoryRepository
                .findByStudentIdOrderBySubmittedAtDesc(studentId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();

        return history;
    }

    @Cacheable(value = "attemptResult", key = "#attemptId")
    public ResultHistoryResponse getAttemptResult(UUID attemptId, UUID studentId) {
        ResultHistory result = resultHistoryRepository.findByAttemptId(attemptId)
                .orElseGet(() -> fetchAndStoreAttempt(attemptId));

        if (result == null || !result.getStudentId().equals(studentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Result not found");
        }

        return toHistoryResponse(result);
    }

    @Cacheable(value = "scoreSummary", key = "#studentId")
    public ScoreSummaryResponse getScoreSummary(UUID studentId) {
        syncResultsFromQuiz(studentId);

        List<ResultHistory> results = resultHistoryRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
        int totalScore = results.stream().map(ResultHistory::getScore).mapToInt(s -> s == null ? 0 : s).sum();
        int totalMaxScore = results.stream().map(ResultHistory::getMaxScore).mapToInt(m -> m == null ? 0 : m).sum();
        double averagePercentage = totalMaxScore == 0 ? 0.0 : (totalScore * 100.0) / totalMaxScore;

        Integer bestScore = results.stream()
                .map(ResultHistory::getScore)
                .filter(s -> s != null)
                .max(Integer::compareTo)
                .orElse(0);

        Double bestPercentage = results.stream()
                .map(ResultHistory::getPercentage)
                .filter(p -> p != null)
                .max(Double::compareTo)
                .orElse(0.0);

        return ScoreSummaryResponse.builder()
                .quizzesAttempted(results.size())
                .totalScore(totalScore)
                .totalMaxScore(totalMaxScore)
                .averagePercentage(round(averagePercentage))
                .bestScore(bestScore)
                .bestPercentage(round(bestPercentage))
                .build();
    }

    public List<ResultHistoryResponse> syncMyResults(UUID studentId) {
        syncResultsFromQuiz(studentId);
        return getMyHistory(studentId);
    }

    /**
     * Look up quiz by title, then build a ranked leaderboard with display names.
     */
    @Cacheable(value = "leaderboard", key = "#title + '-' + #limit")
    public List<LeaderboardEntryResponse> getQuizLeaderboard(String title, int limit) {
        // 1. Resolve title → quizId
        Integer quizId = resolveQuizIdByTitle(title);
        if (quizId == null) {
            throw new  ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }

        // 2. Fetch top results for that quiz
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<ResultHistory> results = resultHistoryRepository
                .findByQuizIdOrderByScoreDescSubmittedAtAsc(quizId, PageRequest.of(0, safeLimit));

        if (results.isEmpty()) {
            return List.of();
        }

        // 3. Batch-resolve studentIds → displayNames
        List<UUID> studentIds = results.stream()
                .map(ResultHistory::getStudentId)
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, String> nameMap = resolveDisplayNames(studentIds);

        // 4. Build response
        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            ResultHistory result = results.get(i);
            String displayName = nameMap.getOrDefault(result.getStudentId(), "Unknown");

            leaderboard.add(LeaderboardEntryResponse.builder()
                    .rank(i + 1)
                    .displayName(displayName)
                    .score(result.getScore())
                    .maxScore(result.getMaxScore())
                    .percentage(result.getPercentage())
                    .submittedAt(result.getSubmittedAt())
                    .submissionMethod(result.getSubmissionMethod())
                    .build());
        }

        return leaderboard;
    }

    // --- private helpers ---

    private Integer resolveQuizIdByTitle(String title) {
        try {
            ResponseEntity<List<QuizInfoResponse>> resp = quizServiceClient.findByTitle(title);
            List<QuizInfoResponse> quizzes = resp.getBody();
            if (quizzes == null || quizzes.isEmpty()) {
                return null;
            }
            // Use first exact match, fall back to first result
            return quizzes.stream()
                    .filter(q -> q.getTitle().equalsIgnoreCase(title))
                    .map(QuizInfoResponse::getId)
                    .findFirst()
                    .orElse(quizzes.get(0).getId());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<UUID, String> resolveDisplayNames(List<UUID> userIds) {
        try {
            ResponseEntity<List<UserDisplayNameResponse>> resp = profileServiceClient.getDisplayNames(userIds);
            List<UserDisplayNameResponse> names = resp.getBody();
            if (names == null) return Map.of();
            return names.stream()
                    .collect(Collectors.toMap(UserDisplayNameResponse::getUserId, UserDisplayNameResponse::getDisplayName));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private void syncResultsFromQuiz(UUID studentId) {
        ResponseEntity<List<QuizAttemptResultResponse>> response = quizAttemptClient.getStudentAttemptResults(studentId);
        List<QuizAttemptResultResponse> attempts = response.getBody();
        if (attempts == null) return;
        attempts.forEach(attempt -> upsertResult(toRecordRequest(attempt)));
    }

    private ResultHistory fetchAndStoreAttempt(UUID attemptId) {
        ResponseEntity<QuizAttemptResultResponse> response = quizAttemptClient.getAttemptResult(attemptId);
        QuizAttemptResultResponse attempt = response.getBody();
        if (attempt == null) return null;
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
                .resultText(formatResult(result))
                .startedAt(result.getStartedAt())
                .submittedAt(result.getSubmittedAt())
                .submissionMethod(result.getSubmissionMethod())
                .build();
    }

    private Double calculatePercentage(Integer score, Integer maxScore) {
        if (score == null || maxScore == null || maxScore == 0) return 0.0;
        return round((score * 100.0) / maxScore);
    }

    private String formatResult(ResultHistory result) {
        if (result.getScore() == null || result.getMaxScore() == null || result.getMaxScore() == 0) return "N/A";
        return result.getScore() + "/" + result.getMaxScore() + " (" + result.getPercentage() + "%)";
    }

    private Double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}