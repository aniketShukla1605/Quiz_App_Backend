package com.microservice.profile_service.repository;

import com.microservice.profile_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository  extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findById(UUID userId);
}
