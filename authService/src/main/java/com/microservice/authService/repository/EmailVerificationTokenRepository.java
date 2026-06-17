package com.microservice.authService.repository;

import com.microservice.authService.entity.EmailVerificationToken;
import com.microservice.authService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenIdAndIsUsed(UUID tokenId, boolean isUsed);

    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.isUsed = true WHERE t.user = :user AND t.isUsed = false")
    void invalidateAllForUser(User user);
}