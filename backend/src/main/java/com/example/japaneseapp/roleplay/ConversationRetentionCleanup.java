package com.example.japaneseapp.roleplay;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Component
public class ConversationRetentionCleanup {
    private final LearningConversationRepository conversationRepository;
    private final Clock clock;

    public ConversationRetentionCleanup(LearningConversationRepository conversationRepository, Clock clock) {
        this.conversationRepository = conversationRepository;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.transcripts.cleanup-delay-ms:600000}")
    @Transactional
    public void deleteExpiredConversations() {
        conversationRepository.deleteAllByExpiresAtLessThanEqual(clock.instant());
    }
}
