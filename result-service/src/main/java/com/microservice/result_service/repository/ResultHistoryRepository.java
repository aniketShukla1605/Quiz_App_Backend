package com.microservice.result_service.repository;

import com.microservice.result_service.entity.ResultHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResultHistoryRepository extends JpaRepository<ResultHistory, Long> {
    Optional<ResultHistory> findByAttemptId(UUID attemptId);
    List<ResultHistory> findByStudentIdOrderBySubmittedAtDesc(UUID studentId);
    List<ResultHistory> findByQuizIdOrderByScoreDescSubmittedAtAsc(Integer quizId, Pageable pageable);
}
