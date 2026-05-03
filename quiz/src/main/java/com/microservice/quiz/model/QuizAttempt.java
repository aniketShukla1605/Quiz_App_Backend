package com.microservice.quiz.model;

import com.microservice.quiz.dto.AnswerEntry;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizAttempt {
    @Id
    private UUID attemptId;
    private int quizId;
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    private AttemptState state; //NOT_STARTED, IN_PROGRESS, SUBMITTED, GRADED

    private LocalDateTime startTime;
    private LocalDateTime submittedAt;
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    private SubmissionMethod submissionMethod; //MANUAL, AUTO

    private Integer score;
    private Integer maxScore;

    @ElementCollection
    @CollectionTable(
            name = "quiz_attempt_answers",
            joinColumns = @JoinColumn(name = "attempt_id")
    )
    private List<AnswerEntry> answers; //{questionId, answer}
}