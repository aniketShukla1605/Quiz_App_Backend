package com.microservice.quiz.repository;

import com.microservice.quiz.model.AttemptState;
import com.microservice.quiz.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    Optional<QuizAttempt> findFirstByQuizIdAndStudentIdAndStateOrderByStartTimeDesc(int quizId, UUID studentId, AttemptState state);
    Optional<QuizAttempt> findFirstByQuizIdAndStudentIdOrderByStartTimeDesc(int quizId, UUID studentId);
    boolean existsByQuizIdAndStudentIdAndState(int quizId, UUID studentId, AttemptState state);
    List<QuizAttempt> findByStudentIdAndStateInOrderBySubmittedAtDesc(UUID studentId, List<AttemptState> states);
}
