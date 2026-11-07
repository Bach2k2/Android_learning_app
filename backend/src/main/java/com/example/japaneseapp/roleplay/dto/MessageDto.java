package com.example.japaneseapp.roleplay.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(UUID id, String role, String text, String translation, String reading, Instant createdAt) {
}
