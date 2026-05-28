package com.microservice.result_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizInfoResponse {
    private Integer id;
    private String title;
    private String category;
}