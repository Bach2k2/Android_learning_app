package com.example.japaneseapp.roleplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateConversationRequest(
        @NotBlank @Size(max = 64) String characterId,
        @NotBlank @Size(max = 64) String scenarioId,
        @NotBlank @Pattern(regexp = "beginner|elementary|intermediate") String level,
        @NotBlank @Pattern(regexp = "ja") String targetLanguage,
        @NotBlank @Pattern(regexp = "en|vi") String translationLanguage
) {
}
