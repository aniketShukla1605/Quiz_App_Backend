package com.microservice.result_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntryResponse {
    private int rank;
    private String displayName;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private LocalDateTime submittedAt;
    private String submissionMethod;
}