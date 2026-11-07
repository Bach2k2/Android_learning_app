package com.example.japaneseapp.roleplay.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationDto(UUID conversationId, Instant createdAt) {
}
