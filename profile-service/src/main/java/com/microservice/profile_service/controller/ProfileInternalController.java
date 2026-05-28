package com.microservice.profile_service.controller;

import com.microservice.profile_service.dto.UserDisplayNameResponse;
import com.microservice.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profile/internal")
@RequiredArgsConstructor
public class ProfileInternalController {

    private final ProfileService profileService;

    @PostMapping("/display-names")
    public ResponseEntity<List<UserDisplayNameResponse>> getDisplayNames(@RequestBody List<UUID> userIds) {
        return profileService.getDisplayNames(userIds);
    }
}