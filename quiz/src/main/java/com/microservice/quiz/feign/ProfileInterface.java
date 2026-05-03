package com.microservice.quiz.feign;

import com.microservice.quiz.dto.QuizResultEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("PROFILE")
public interface ProfileInterface {
    @PostMapping("/profile/internal/history")
    void recordQuizResult(@RequestBody QuizResultEvent event);
}