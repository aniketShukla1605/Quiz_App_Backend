package com.microservice.quiz.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmitRequest {
    private UUID attemptId;
    private List<AnswerEntry> answers;
}