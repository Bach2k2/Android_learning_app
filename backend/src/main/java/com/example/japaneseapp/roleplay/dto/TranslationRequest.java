package com.example.japaneseapp.roleplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TranslationRequest(
        @NotBlank @Size(max = 2000) String text,
        @NotBlank @Pattern(regexp = "ja") String sourceLanguage,
        @NotBlank @Pattern(regexp = "en|vi") String targetLanguage,
        @Size(max = 1000) String context
) {
}
