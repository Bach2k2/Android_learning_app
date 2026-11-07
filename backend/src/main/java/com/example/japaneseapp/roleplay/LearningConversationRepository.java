package com.example.japaneseapp.roleplay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LearningConversationRepository extends JpaRepository<LearningConversation, UUID> {
    Optional<LearningConversation> findByIdAndUser_IdAndExpiresAtAfter(UUID id, UUID userId, Instant now);
    long deleteAllByExpiresAtLessThanEqual(Instant now);
}
