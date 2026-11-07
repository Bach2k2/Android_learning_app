package com.example.japaneseapp.roleplay;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, UUID> {
    List<ConversationMessage> findFirst51ByConversation_IdOrderByCreatedAtDescIdDesc(UUID conversationId);

    @Query("""
            select m from ConversationMessage m
            where m.conversation.id = :conversationId
              and (m.createdAt < :createdAt or (m.createdAt = :createdAt and m.id < :messageId))
            order by m.createdAt desc, m.id desc
            """)
    List<ConversationMessage> findHistoryBefore(
            @Param("conversationId") UUID conversationId,
            @Param("createdAt") Instant createdAt,
            @Param("messageId") UUID messageId,
            Pageable pageable);

    Optional<ConversationMessage> findByConversation_IdAndClientMessageIdAndRole(
            UUID conversationId, UUID clientMessageId, String role);
}
