package com.microservice.quiz.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SyncRequest {
    private UUID attemptId;
    private List<AnswerEntry> currentAnswers;
    private LocalDateTime clientTime;
}