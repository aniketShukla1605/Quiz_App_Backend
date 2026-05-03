package com.microservice.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncResponse {
    private LocalDateTime serverTime;
    private LocalDateTime expiryTime;
    private String submissionStatus;
    //populated only when quiz was auto-submitted during disconnection
    private Integer score;
    private Integer maxScore;
    private LocalDateTime submittedAt;
    private String submissionMethod;
}
