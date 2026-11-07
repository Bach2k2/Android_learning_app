package com.example.japaneseapp.roleplay;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.*;

class ConversationRetentionCleanupTest {
    @Test
    void scheduledCleanupDeletesConversationsAtOrPastExpiry() {
        LearningConversationRepository repository = mock(LearningConversationRepository.class);
        Instant now = Instant.parse("2026-10-31T09:00:00Z");
        ConversationRetentionCleanup cleanup = new ConversationRetentionCleanup(
                repository, Clock.fixed(now, ZoneOffset.UTC));

        cleanup.deleteExpiredConversations();

        verify(repository).deleteAllByExpiresAtLessThanEqual(now);
    }
}
