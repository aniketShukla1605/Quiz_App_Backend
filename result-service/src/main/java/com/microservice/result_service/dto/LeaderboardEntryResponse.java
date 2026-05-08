package com.microservice.result_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardEntryResponse {
    private int rank;
    private UUID studentId;
    private Integer score;
    private Integer maxScore;
    private Double percentage;
    private LocalDateTime submittedAt;
    private String submissionMethod;
}
