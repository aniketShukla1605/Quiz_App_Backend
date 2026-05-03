package com.microservice.quiz.service;

import com.microservice.quiz.dto.*;
import com.microservice.quiz.feign.QuizInterface;
import com.microservice.quiz.model.AttemptState;
import com.microservice.quiz.model.Quiz;
import com.microservice.quiz.model.QuizAttempt;
import com.microservice.quiz.model.SubmissionMethod;
import com.microservice.quiz.repository.QuizAttemptRepository;
import com.microservice.quiz.repository.QuizRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final QuizRepo quizRepo;
    private final QuizInterface quizInterface;

    private static final int DEFAULT_DURATION_MINUTES = 30;

    //Start
    public ResponseEntity<?> startQuiz(int quizId, UUID studentId) {

        Quiz quiz = quizRepo.findById(quizId).orElse(null);
        if (quiz == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Quiz not found");
        }

        var existing = attemptRepository.findByQuizIdAndStudentId(quizId, studentId).orElse(null);
        if (existing != null) {
            if (existing.getState() == AttemptState.IN_PROGRESS) {
                //return current state instead of error
                return buildStartResponse(existing, quiz);
            }
            //if already submitted or graded
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Quiz already submitted at " + existing.getSubmittedAt());
        }

        int duration = quiz.getDurationMinutes() != null ? quiz.getDurationMinutes() : DEFAULT_DURATION_MINUTES;

        LocalDateTime now = LocalDateTime.now();

        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(quizId)
                .studentId(studentId)
                .state(AttemptState.IN_PROGRESS)
                .startTime(now)
                .expiryTime(now.plusMinutes(duration))
                .build();

        attemptRepository.save(attempt);

        return buildStartResponse(attempt, quiz);
    }

    private ResponseEntity<?> buildStartResponse(QuizAttempt attempt, Quiz quiz) {
        int duration = quiz.getDurationMinutes() != null ? quiz.getDurationMinutes() : DEFAULT_DURATION_MINUTES;

        //Fetch questions via Feign
        List<QuestionDto> questions = quizInterface
                .getQuestionsFromId(quiz.getQuestionId())
                .getBody();

        StartQuizResponse response = StartQuizResponse.builder()
                .attemptId(attempt.getAttemptId())
                .quizId(attempt.getQuizId())
                .state(attempt.getState().name())
                .startTime(attempt.getStartTime())
                .durationMinutes(duration)
                .serverTime(LocalDateTime.now())
                .expiryTime(attempt.getExpiryTime())
                .questions(questions)
                .build();

        return ResponseEntity.ok(response);
    }

    //Sync
    public ResponseEntity<?> syncQuiz(int quizId, UUID studentId, SyncRequest request) {

        QuizAttempt attempt = attemptRepository.findByQuizIdAndStudentId(quizId, studentId)
                .orElse(null);

        if (attempt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attempt not found");
        }

        //If already submitted during disconnection, return submission result
        if (attempt.getState() == AttemptState.GRADED || attempt.getState() == AttemptState.SUBMITTED) {
            return ResponseEntity.ok(SyncResponse.builder()
                    .serverTime(LocalDateTime.now())
                    .expiryTime(attempt.getExpiryTime())
                    .submissionStatus(attempt.getState().name())
                    .score(attempt.getScore())
                    .maxScore(attempt.getMaxScore())
                    .submittedAt(attempt.getSubmittedAt())
                    .submissionMethod(attempt.getSubmissionMethod().name())
                    .build());
        }

        //Already submitted —(409)
        if (attempt.getState() != AttemptState.IN_PROGRESS) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Quiz already submitted");
        }

        //set answers in attempt
        if (request.getCurrentAnswers() != null) {
            attempt.setAnswers(request.getCurrentAnswers());
        }

        //check if time is up
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(attempt.getExpiryTime())) {
            return autoSubmit(attempt);
        }

        attemptRepository.save(attempt);

        return ResponseEntity.ok(SyncResponse.builder()
                .serverTime(now)
                .expiryTime(attempt.getExpiryTime())
                .submissionStatus(attempt.getState().name())
                .build());
    }

    //SUBMIT
    public ResponseEntity<?> submitQuiz(int quizId, UUID studentId, SubmitRequest request) {

        QuizAttempt attempt = attemptRepository.findByQuizIdAndStudentId(quizId, studentId)
                .orElse(null);

        if (attempt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attempt not found");
        }

        //already submitted
        if (attempt.getState() == AttemptState.GRADED || attempt.getState() == AttemptState.SUBMITTED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(SubmitResponse.builder()
                            .attemptId(attempt.getAttemptId())
                            .quizId(attempt.getQuizId())
                            .state(attempt.getState().name())
                            .score(attempt.getScore())
                            .maxScore(attempt.getMaxScore())
                            .submittedAt(attempt.getSubmittedAt())
                            .submissionMethod(attempt.getSubmissionMethod().name())
                            .build());
        }

        if (attempt.getState() != AttemptState.IN_PROGRESS) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quiz is not in progress");
        }

        try {
            //Save answers first
            attempt.setAnswers(request.getAnswers());

            //Calculate score from Question Service
            List<Response> responses = request.getAnswers().stream()
                    .map(a -> new Response(a.getQuestionId(), a.getAnswer()))
                    .collect(Collectors.toList());

            Integer score = quizInterface.getScore(responses).getBody();

            //Get total questions as max score
            Quiz quiz = quizRepo.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));
            int maxScore = quiz.getQuestionId().size();

            //check if manually submitted or not
            LocalDateTime now = LocalDateTime.now();
            SubmissionMethod method = now.isBefore(attempt.getExpiryTime()) ? SubmissionMethod.MANUAL : SubmissionMethod.AUTO;

            attempt.setState(AttemptState.GRADED);
            attempt.setScore(score);
            attempt.setMaxScore(maxScore);
            attempt.setSubmittedAt(now);
            attempt.setSubmissionMethod(method);

            attemptRepository.save(attempt);

            return ResponseEntity.ok(SubmitResponse.builder()
                    .attemptId(attempt.getAttemptId())
                    .quizId(attempt.getQuizId())
                    .state(attempt.getState().name())
                    .score(score)
                    .maxScore(maxScore)
                    .submittedAt(now)
                    .submissionMethod(method.name())
                    .build());

        } catch (ObjectOptimisticLockingFailureException e) {
            //return existing result
            QuizAttempt updated = attemptRepository.findByQuizIdAndStudentId(quizId, studentId)
                    .orElseThrow();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(SubmitResponse.builder()
                            .attemptId(updated.getAttemptId())
                            .quizId(updated.getQuizId())
                            .state(updated.getState().name())
                            .score(updated.getScore())
                            .maxScore(updated.getMaxScore())
                            .submittedAt(updated.getSubmittedAt())
                            .submissionMethod(updated.getSubmissionMethod().name())
                            .build());
        }
    }

    //AUTO SUBMIT
    private ResponseEntity<?> autoSubmit(QuizAttempt attempt) {
        try {
            List<Response> responses = attempt.getAnswers() == null ? List.of() : attempt.getAnswers().stream()
                    .map(a -> new Response(a.getQuestionId(), a.getAnswer()))
                    .collect(Collectors.toList());

            Integer score = responses.isEmpty() ? 0 : quizInterface.getScore(responses).getBody();

            Quiz quiz = quizRepo.findById(attempt.getQuizId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));
            int maxScore = quiz.getQuestionId().size();

            LocalDateTime now = LocalDateTime.now();
            attempt.setState(AttemptState.GRADED);
            attempt.setScore(score);
            attempt.setMaxScore(maxScore);
            attempt.setSubmittedAt(now);
            attempt.setSubmissionMethod(SubmissionMethod.AUTO);

            attemptRepository.save(attempt);

            return ResponseEntity.ok(SyncResponse.builder()
                    .serverTime(now)
                    .expiryTime(attempt.getExpiryTime())
                    .submissionStatus(AttemptState.GRADED.name())
                    .score(score)
                    .maxScore(maxScore)
                    .submittedAt(now)
                    .submissionMethod(SubmissionMethod.AUTO.name())
                    .build());

        } catch (ObjectOptimisticLockingFailureException e) {
            QuizAttempt updated = attemptRepository
                    .findByQuizIdAndStudentId(attempt.getQuizId(), attempt.getStudentId())
                    .orElseThrow();
            return ResponseEntity.ok(SyncResponse.builder()
                    .serverTime(LocalDateTime.now())
                    .expiryTime(updated.getExpiryTime())
                    .submissionStatus(updated.getState().name())
                    .score(updated.getScore())
                    .maxScore(updated.getMaxScore())
                    .submittedAt(updated.getSubmittedAt())
                    .submissionMethod(updated.getSubmissionMethod().name())
                    .build());
        }
    }
}