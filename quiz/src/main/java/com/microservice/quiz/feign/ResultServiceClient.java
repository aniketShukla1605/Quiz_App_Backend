package com.microservice.quiz.feign;

import com.microservice.quiz.dto.ResultRecordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "result-service")
public interface ResultServiceClient {

    @PostMapping("/results/internal/record")
    ResponseEntity<Void> recordResult(@RequestBody ResultRecordRequest request);
}
