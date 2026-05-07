package com.microservice.result_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreSummaryResponse {
    private long quizzesAttempted;
    private Integer totalScore;
    private Integer totalMaxScore;
    private Double averagePercentage;
    private Integer bestScore;
    private Double bestPercentage;
}
