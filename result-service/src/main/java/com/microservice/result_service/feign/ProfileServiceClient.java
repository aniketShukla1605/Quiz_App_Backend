package com.microservice.result_service.feign;

import com.microservice.result_service.dto.UserDisplayNameResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "profile-service")
public interface ProfileServiceClient {

    @PostMapping("/profile/internal/display-names")
    ResponseEntity<List<UserDisplayNameResponse>> getDisplayNames(@RequestBody List<UUID> userIds);
}