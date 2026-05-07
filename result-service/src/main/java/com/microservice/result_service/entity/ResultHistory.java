package com.microservice.result_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID attemptId;

    //quizId and studentId can not be null
    @Column(nullable = false)
    private Integer quizId;

    @Column(nullable = false)
    private UUID studentId;

    private String state;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private String submissionMethod;
    private LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
}
